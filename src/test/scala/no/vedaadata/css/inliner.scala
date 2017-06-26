package no.vedaadata.css

import org.scalatest._

class CssInlinerTest extends FunSuite with Matchers {

  //  Selectors

  test("all selector") {

    val xhtml = <div/>

    val css =
      """
        * { color: blue; }
      """

    val expected = <div style="color: blue;"/>

    CssInliner inline(xhtml, css) shouldEqual expected
  }

  test("element selector") {

    val xhtml = <div/>

    val css =
      """
        div { color: blue; }
      """

    val expected = <div style="color: blue;"/>

    CssInliner inline(xhtml, css) shouldEqual expected
  }

  test("id selector") {

    val xhtml = <div id="foo"/>

    val css =
      """
        #foo { color: blue; }
      """

    val expected = <div id="foo" style="color: blue;"/>

    CssInliner inline(xhtml, css) shouldEqual expected
  }

  test("class selector") {

    val xhtml = <div class="foo"/>

    val css =
      """
        .foo { color: blue; }
      """

    val expected = <div class="foo" style="color: blue;"/>

    CssInliner inline(xhtml, css) shouldEqual expected
  }

  test("class selector with element") {

    val xhtml =
      <div>
        <div class="foo"/>
        <p class="foo"/>
      </div>

    val css =
      """
        p.foo { color: blue; }
      """

    val expected =
      <div>
        <div class="foo"/>
        <p class="foo" style="color: blue;"/>
      </div>

    CssInliner inline(xhtml, css) shouldEqual expected
  }

  test("class selector with multiple classes") {

    val xhtml =
      <div>
        <div class="foo"/>
        <div class="foo bar"/>
      </div>

    val css =
      """
        .foo.bar { color: blue; }
      """

    val expected =
      <div>
        <div class="foo"/>
        <div class="foo bar" style="color: blue;"/>
      </div>

    CssInliner inline(xhtml, css) shouldEqual expected
  }

  //  Combinations

  test("combine rules") {

    val xhtml = <div id="foo"/>

    val css =
      """
        div { color: blue; }
        div { margin: 10px; }
      """

    val expected = <div id="foo" style="color: blue; margin: 10px;"/>

    CssInliner inline(xhtml, css) shouldEqual expected
  }

  //  Iterations

  test("applying same rules twice should yield same result") {

    val xhtml = <div/>

    val css =
      """
        * { color: blue; }
      """

    val expected = <div style="color: blue;"/>

    CssInliner inline(CssInliner inline(xhtml, css), css) shouldEqual expected

  }

  //  General transformation

  test("other nodes are passed through unharmed") {

    val xhtml =
      <div>
        <p>hello</p>
      </div>

    val css =
      """
        div { color: blue; }
      """

    val expected =
      <div style="color: blue;">
        <p>hello</p>
      </div>

    CssInliner inline(xhtml, css) shouldEqual expected
  }

  test("nested element") {

    val xhtml =
      <div>
        <div/>
      </div>

    val css =
      """
        div { color: blue; }
      """

    val expected =
      <div style="color: blue;">
        <div style="color: blue;"/>
      </div>

    CssInliner inline(xhtml, css) shouldEqual expected
  }

}

