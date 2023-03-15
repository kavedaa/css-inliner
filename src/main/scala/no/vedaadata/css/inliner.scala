package no.vedaadata.css

import cssparse.Ast._
import cssparse._
import fastparse._

import scala.xml.{ Elem, Node, NodeSeq, Text, UnprefixedAttribute }

object CssInliner {

  def apply(css: String) = {

//    val parser = CssRulesParser.stylesheet

    val res = parse(css, CssRulesParser.ruleList(_))

    //    println(res)

    res match {
      case Parsed.Success(value, index) =>
        value.rules foreach {
          case QualifiedRule(selector, block) =>
            selector match {
              case Left(value) =>
              //                  println(value)
              //                  println(PrettyPrinter.printDeclarationList(block, 0, false).trim)
              case _ => ???
            }
          case _ => ???
        }        
      case _ => ???
    }
  }

  def inline(xhtml: Elem, css: String): Elem = {

    val selectorDeclarations = parse(css, CssRulesParser.ruleList(_)) match {
      case Parsed.Success(value, _) =>
        value.rules collect {
          case QualifiedRule(Left(selector), block) => (selector, block)
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
        throw new Exception("Attribute CSS selectors not supported yet.")

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
            case PseudoSelectorPart(pseudoClass, param) => 
              throw new Exception("Pseudo CSS selectors not supported yet.")
          })

      case MultipleSelector(firstSelector, selectors) =>
        throw new Exception("Multiple CSS selectors e.g. 'div div' not supported yet.")
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
  val traverser = new NodeTransformer({ case elem: Elem => elem.copy(child = elem.child.map(transform).flatten) })

  override def transform(node: Node): NodeSeq = super.transform(node).map(traverser.transform).flatten
}
