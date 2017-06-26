package no.vedaadata.css

import cssparse.Ast._
import cssparse.{CssRulesParser, PrettyPrinter}
import fastparse.core.Parsed

import scala.xml._

object CssInliner {

  def apply(css: String) = {

    val parser = CssRulesParser.stylesheet

    val res = parser parse css

    //    println(res)

    res match {
      case Parsed.Success(value, index) =>
        value.rules foreach {
          case Left(rule) => rule match {
            case QualifiedRule(selector, block) =>
              selector match {
                case Left(value) =>
                //                  println(value)
                //                  println(PrettyPrinter.printDeclarationList(block, 0, false).trim)
                case _ => ???
              }
            case _ => ???
          }
          case Right(x) => println(x)
        }
      case _ => ???
    }
  }

  def inline(xhtml: Elem, css: String): Elem = {

    val selectorDeclarations = CssRulesParser.stylesheet parse css match {
      case Parsed.Success(value, _) =>
        value.rules collect {
          case Left(QualifiedRule(Left(selector), block)) => (selector, block)
        }
      case _ => Nil
    }

    def declarationListsForElem(elem: Elem) = selectorDeclarations collect {
      case (selector, block) if selectorMatchesElem(selector, elem) => block
    }

    def selectorMatchesElem(selector: Selector, elem: Elem): Boolean = selector match {

      case AllSelector() =>
        true

      case ElementSelector(name) =>
        name == elem.label

      case IdSelector(id) =>
        elem attribute "id" exists { case Text(x) => x == id; case _ => false }

      case AttributeSelector(name, attrs) =>
        ???

      case ComplexSelector(firstPart, parts) =>
        (firstPart forall { part => selectorMatchesElem(part, elem) }) &&
          (parts forall {
            case ClassSelectorPart(part) => part match {
              case AllSelector() =>
                true
              case ElementSelector(name) =>
                elem attribute "class" exists { case Text(x) => x split " " contains name; case _ => false }
              case _ =>
                false
            }
            case PseudoSelectorPart(pseudoClass, param) => ???
          })

      case MultipleSelector(firstSelector, selectors) =>
        ???

      case _ =>
        false
    }

    val transformation: PartialFunction[Node, NodeSeq] = {
      case elem: Elem =>
        val lists = declarationListsForElem(elem)
        val combined = DeclarationList(lists flatMap(_.declarations))
        val styles = PrettyPrinter.printDeclarationList(combined, 0, false).trim
        if (styles.nonEmpty) elem copy (attributes = elem.attributes append new UnprefixedAttribute("style", styles, xml.Null))
        else elem
    }


    class InliningTransformer extends NodeTransformer(transformation) with Traversive

    new InliningTransformer transform xhtml match {
      case elem: Elem => elem
      case nodeSeq: NodeSeq => nodeSeq.head match {
        case elem: Elem => elem
        case _ => throw new Exception("Unexpected result of transformation.")
      }
      case _ => throw new Exception("Unexpected result of transformation.")
    }
  }

}


abstract class Transformer {
  protected val default: PartialFunction[Node, NodeSeq] = {
    case node => node
  }
  val pfs: Seq[PartialFunction[Node, NodeSeq]]
  val chained: PartialFunction[Node, NodeSeq] = pfs :+ default reduceLeft (_ orElse _)

  def transform(node: Node): NodeSeq = chained(node)
}

class NodeTransformer(val pfs: PartialFunction[Node, NodeSeq]*) extends Transformer

trait Traversive extends Transformer {
  val traverser = new NodeTransformer({ case elem: Elem => elem copy (child = elem.child map transform flatten) })

  override def transform(node: Node): NodeSeq = super.transform(node) map traverser.transform flatten
}
