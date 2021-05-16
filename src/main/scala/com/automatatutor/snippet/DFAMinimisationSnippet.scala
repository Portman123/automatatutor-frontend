/*package com.automatatutor.snippet
import com.automatatutor.lib.GraderConnection
import com.automatatutor.model.{Problem, RegExConstructionProblem, RegexConstructionSolutionAttempt, SolutionAttempt}
import net.liftweb.common.{Box, Full}
import net.liftweb.http.{SHtml, Templates}
import net.liftweb.http.js.JE.{Call, JsRaw}
import net.liftweb.http.js.{JsCmd, JsCmds}
import net.liftweb.http.js.JsCmds.{JsHideId, JsIf, JsShowId, SetHtml}
import net.liftweb.util.Helpers

import java.util.{Calendar, Date}
import scala.xml.{NodeSeq, Text, XML}


//test modules
import java.util.Calendar
import java.util.Date
import scala.Array.canBuildFrom
import scala.Array.fallbackCanBuildFrom
import scala.xml.NodeSeq
import scala.xml.NodeSeq.seqToNodeSeq
import scala.xml.Text
import scala.xml.XML
import com.automatatutor.lib.GraderConnection
import com.automatatutor.model.Problem
import com.automatatutor.model.RegExConstructionProblem
import com.automatatutor.model.RegexConstructionSolutionAttempt
import com.automatatutor.model.SolutionAttempt
import net.liftweb.common.Box
import net.liftweb.common.Full
import net.liftweb.http.SHtml
import net.liftweb.http.SHtml.ElemAttr.pairToBasic
import net.liftweb.http.Templates
import net.liftweb.http.js.JE.JsRaw
import net.liftweb.http.js.JsCmd
import net.liftweb.http.js.JsCmds
import net.liftweb.http.js.JsCmds._
import net.liftweb.http.js.JsCmds.JsHideId
import net.liftweb.http.js.JsCmds.JsShowId
import net.liftweb.http.js.JsCmds.SetHtml
import net.liftweb.http.js.JsCmds.cmdToString
import net.liftweb.http.js.JsCmds.jsExpToJsCmd
import net.liftweb.util.Helpers
import net.liftweb.util.Helpers._
import net.liftweb.util.Helpers.strToSuperArrowAssoc
import net.liftweb.http.js.JE.Call
import net.liftweb.common.Empty
//end test modules
*/

/*
object DFAMinimisationSnippet extends ProblemSnippet {


/** Should produce a NodeSeq that allows the user to create a new problem of
 * the type. This NodeSeq also has to handle creation of the unspecific
 * {@link Problem}. */
override def renderCreate(createUnspecificProb: (String, String) => Problem, returnFunc: () => Nothing): NodeSeq = {
  ???
}


/** Should produce a NodeSeq that allows the user to edit the problem
 * associated with the given unspecific problem. */
override def renderEdit: Box[(Problem, () => Nothing) => NodeSeq] = ???

/** Should produce a NodeSeq that allows the user a try to solve the problem
 * associated with the given unspecific problem. The function
 * recordSolutionAttempt must be called once for every solution attempt
 * and expects the grade of the attempt (which must be <= maxGrade) and the
 * time the attempt was made. After finishing the solution attempt, the
 * snippet should send the user back to the overview of problems in the
 * set by calling returnToSet */
override def renderSolve(problem: Problem, maxGrade: Long, lastAttempt: Box[SolutionAttempt], recordSolutionAttempt: (Int, Date) => SolutionAttempt, returnToSet: () => Unit, attemptsLeft: () => Int, bestGrade: () => Int): NodeSeq = ???

/** Is called before the given unspecific problem is deleted from the database.
 * This method should delete everything associated with the given unspecific
 * problem from the database */
override def onDelete(problem: Problem): Unit = ???
}

*/


// experiment
package com.automatatutor.snippet

import java.util.Calendar
import java.util.Date
import scala.xml.NodeSeq
import scala.xml.Text
import scala.xml.XML
import com.automatatutor.lib.GraderConnection
import com.automatatutor.model.{DFAMinSolutionAttempt, DFAMinimisationProblem, Problem, SolutionAttempt}
import net.liftweb.http.js.JE.JsRaw
import net.liftweb.http.js.JsCmd
import net.liftweb.http.js.JsCmds
import net.liftweb.http.js.JsCmds.JsHideId
import net.liftweb.http.js.JsCmds.JsShowId
import net.liftweb.http.js.JsCmds.SetHtml
import net.liftweb.http.js.JsCmds.cmdToString
import net.liftweb.http.js.JsCmds.jsExpToJsCmd
import net.liftweb.common.Box
import net.liftweb.common.Full
import net.liftweb.http.S
import net.liftweb.http.SHtml
import net.liftweb.http.Templates
import net.liftweb.mapper.By
import net.liftweb.util.Helpers
import net.liftweb.util.Helpers._

object DFAMinimisationSnippet extends ProblemSnippet {
  def preprocessAutomatonXml ( input : String ) : String = {
    val withoutNewlines = input.filter(!List('\n', '\r').contains(_)).replace("\u0027", "\'")
    val asXml = XML.loadString(withoutNewlines)
    val symbolsWithoutEpsilon = (asXml \ "alphabet" \ "symbol").filter(node => node.text != "ε")
    val alphabetWithoutEpsilon = <alphabet> { symbolsWithoutEpsilon } </alphabet>
    val automatonWithoutAlphabet = asXml.child.filter(_.label != "alphabet")
    val newAutomaton = <automaton> { alphabetWithoutEpsilon } { automatonWithoutAlphabet } </automaton>
    return newAutomaton.toString.replace("\"","\'")
  }

  /** Should produce a NodeSeq that allows the user to create a new problem of
   * the type. This NodeSeq also has to handle creation of the unspecific
   * {@link Problem}. */
  override def renderCreate( createUnspecificProb : (String, String) => Problem,
                             returnFunc : () => Nothing ) : NodeSeq = {

    var automaton : String = ""
    var shortDescription : String = ""

    def create() = {
      val longDescription = "Construct a DFA that is equivalent and minimal"
      val unspecificProblem = createUnspecificProb(shortDescription, longDescription)

      val specificProblem : DFAMinimisationProblem = DFAMinimisationProblem.create
      specificProblem.setGeneralProblem(unspecificProblem).setAutomaton(automaton)
      specificProblem.save

      returnFunc()
    }

    // Remember to remove all newlines from the generated XML by using filter. Also remove 'ε' from the alphabet, as its implied
    val automatonField = SHtml.hidden(automatonXml => automaton = preprocessAutomatonXml(automatonXml), "", "id" -> "automatonField")
    val shortDescriptionField = SHtml.text("", shortDescription = _)
    val submitButton = SHtml.submit("Create", create, "onClick" -> "document.getElementById('automatonField').value = Editor.canvas.exportAutomaton()")


    val template : NodeSeq = Templates(List("dfa-minimisation-problem", "create")) openOr Text("Could not find template /dfa-minimisation-problem/create")
    Helpers.bind("createform", template,
      "automaton" -> automatonField,
      "shortdescription" -> shortDescriptionField,
      "submit" -> submitButton
    )
  }

  /** Should produce a NodeSeq that allows the user to edit the problem
   * associated with the given unspecific problem. */
  override def renderEdit : Box[(Problem, () => Nothing) => NodeSeq] = Full(renderEditFunc)

  private def renderEditFunc(problem : Problem, returnFunc : () => Nothing) : NodeSeq = {
    val dfaMinimisationProblem = DFAMinimisationProblem.findByGeneralProblem(problem)

    var shortDescription : String = problem.getShortDescription
    var automaton : String = ""

    def create() = {
      problem.setShortDescription(shortDescription).save()
      dfaMinimisationProblem.setAutomaton(automaton).save()
      returnFunc()
    }

    // Remember to remove all newlines from the generated XML by using filter
    val automatonField = SHtml.hidden(automatonXml => automaton = preprocessAutomatonXml(automatonXml), "", "id" -> "automatonField")
    val shortDescriptionField = SHtml.text(shortDescription, shortDescription = _)
    val submitButton = SHtml.submit("Edit", create, "onClick" -> "document.getElementById('automatonField').value = Editor.canvas.exportAutomaton()")
    val setupScript =
      <script type="text/javascript">
        Editor.canvas.setAutomaton( "{ preprocessAutomatonXml(dfaMinimisationProblem.getAutomaton) }" );
      </script>


    val template : NodeSeq = Templates(List("dfa-minimisation-problem", "edit")) openOr Text("Could not find template /dfa-minimisation-problem/edit")
    Helpers.bind("editform", template,
      "automaton" -> automatonField,
      "setupscript" -> setupScript,
      "shortdescription" -> shortDescriptionField,
      "submit" -> submitButton)
  }

  /** Should produce a NodeSeq that allows the user a try to solve the problem
   * associated with the given unspecific problem. The function
   * recordSolutionAttempt must be called once for every solution attempt
   * and expects the grade of the attempt (which must be <= maxGrade) and the
   * time the attempt was made. After finishing the solution attempt, the
   * snippet should send the user back to the overview of problems in the
   * set by calling returnToSet */
  override def renderSolve ( generalProblem : Problem, maxGrade : Long, lastAttempt : Box[SolutionAttempt],
                             recordSolutionAttempt: (Int, Date)  => SolutionAttempt,
                             returnFunc : () => Unit, remainingAttempts : () => Int, bestGrade : () => Int ) : NodeSeq = {

    // Get problem from database
    val dfaMinimisationProblem = DFAMinimisationProblem.findByGeneralProblem(generalProblem)

    // Check remaining attempts. If none left, return best grade and a nice little message ;)
    def grade( attemptDfaDescription : String ) : JsCmd = {
      if(remainingAttempts() <= 0) {
        return JsShowId("feedbackdisplay") & SetHtml("feedbackdisplay",
          Text("You do not have any attempts left for this problem. Your final grade is " +
            bestGrade().toString + "/" + maxGrade.toString + "."))
      }

      val attemptDfaXml = XML.loadString(attemptDfaDescription)
      val correctDfaDescription = dfaMinimisationProblem.getXmlDescription.toString
      val attemptTime = Calendar.getInstance.getTime()
      val graderResponse = GraderConnection.getDfaMinimisationFeedback(correctDfaDescription, attemptDfaDescription, maxGrade.toInt)

      val numericalGrade = graderResponse._1
      val generalAttempt = recordSolutionAttempt(numericalGrade, attemptTime)

      // Only save the specific attempt if we saved the general attempt
      if (generalAttempt != null) {
        DFAMinSolutionAttempt.create.solutionAttemptId(generalAttempt).attemptAutomaton(attemptDfaDescription).save
      }

      val setNumericalGrade : JsCmd = SetHtml("grade", Text(graderResponse._1.toString + "/" + maxGrade.toString))
      val setFeedback : JsCmd = SetHtml("feedback", graderResponse._2)
      val showFeedback : JsCmd = JsShowId("feedbackdisplay")

      return setNumericalGrade & setFeedback & showFeedback & JsCmds.JsShowId("submitbutton")
    }

    val problemAlphabet = dfaMinimisationProblem.getAlphabet
    val problemAlphabetNodeSeq = Text("{" + problemAlphabet.mkString(",") + "}")
    val problemDescriptionNodeSeq = Text(generalProblem.getLongDescription)

    val hideSubmitButton : JsCmd = JsHideId("submitbutton")
    val ajaxCall : JsCmd = SHtml.ajaxCall(JsRaw("Editor.canvasMin.exportAutomaton()"), grade(_))
    val submitButton : NodeSeq = <button type='button' id='submitbutton' onclick={hideSubmitButton & ajaxCall}>Submit</button>

    val returnLink : NodeSeq = SHtml.link("/courses/show", returnFunc, Text("Return to Course"))
    val alphabetJavaScriptArray = "[\"" + problemAlphabet.mkString("\",\"") + "\"]"

    val setupScript : NodeSeq =
      <script type="text/javascript">
        Editor.canvasDfa.setAutomaton( "{ preprocessAutomatonXml(dfaMinimisationProblem.getXmlDescription.toString) }" );
        Editor.canvasDfa.lockCanvas();
        Editor.canvasMin.setAlphabet( { alphabetJavaScriptArray } );
      </script>



    val template : NodeSeq = Templates(List("dfa-minimisation-problem", "solve")) openOr Text("Template /dfa-minimisation-problem/solve not found")
    return SHtml.ajaxForm(Helpers.bind("nfatodfaform", template,
      "setupscript" -> setupScript,
      "returnlink" -> returnLink,
      "submitbutton" -> submitButton))

  }

  /** Is called before the given unspecific problem is deleted from the database.
   * This method should delete everything associated with the given unspecific
   * problem from the database */
  override def onDelete( problem : Problem ) : Unit = {
    DFAMinimisationProblem.deleteByGeneralProblem(problem)
  }
}