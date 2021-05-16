package com.automatatutor.snippet

import java.util.Calendar
import java.util.Date
import scala.Array.canBuildFrom
import scala.Array.fallbackCanBuildFrom
import scala.xml.NodeSeq
import scala.xml.NodeSeq.seqToNodeSeq
import scala.xml.Text
import scala.xml.XML
import com.automatatutor.lib.GraderConnection
import com.automatatutor.model.{DFAConstructionProblem, DFAConstructionSolutionAttempt, Problem, RegexpToDFAProblem, RegexpToDFASolutionAttempt, SolutionAttempt}
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

object RegexpToDFASnippet extends ProblemSnippet {
  /** Should produce a NodeSeq that allows the user to create a new problem of
   * the type. This NodeSeq also has to handle creation of the unspecific
   * {@link Problem}. */

  //override def renderCreate(createUnspecificProb: (String, String) => Problem, returnFunc: () => Nothing): NodeSeq = ???

  /** Should produce a NodeSeq that allows the user to edit the problem
   * associated with the given unspecific problem. */

  //override def renderEdit: Box[(Problem, () => Nothing) => NodeSeq] = ???

  /** Should produce a NodeSeq that allows the user a try to solve the problem
   * associated with the given unspecific problem. The function
   * recordSolutionAttempt must be called once for every solution attempt
   * and expects the grade of the attempt (which must be <= maxGrade) and the
   * time the attempt was made. After finishing the solution attempt, the
   * snippet should send the user back to the overview of problems in the
   * set by calling returnToSet */

  //override def renderSolve(problem: Problem, maxGrade: Long, lastAttempt: Box[SolutionAttempt], recordSolutionAttempt: (Int, Date) => SolutionAttempt, returnToSet: () => Unit, attemptsLeft: () => Int, bestGrade: () => Int): NodeSeq = ???

  /** Is called before the given unspecific problem is deleted from the database.
   * This method should delete everything associated with the given unspecific
   * problem from the database */

  //override def onDelete(problem: Problem): Unit = ???

  def preprocessAutomatonXml ( input : String ) : String = input.filter(!List('\n', '\r').contains(_)).replace("\u0027", "\'")

  override def renderCreate( createUnspecificProb : (String, String) => Problem,
                             returnFunc : () => Nothing ) : NodeSeq = {
    /** Should produce a NodeSeq that allows the user to create a new problem of
     * the type. This NodeSeq also has to handle creation of the unspecific
     * {@link Problem}. */
    def create(formValues : String) : JsCmd = {
      // store webapp field contents in variables?
      val formValuesXml = XML.loadString(formValues)
      val regEx = (formValuesXml \ "regexfield").head.text
      val alphabet = (formValuesXml \ "alphabetfield").head.text
      val shortDescription = (formValuesXml \ "shortdescfield").head.text
      val longDescription = (formValuesXml \ "longdescfield").head.text

      //Keep only the chars
      val alphabetList = alphabet.split(" ").filter(_.length()>0);
      val parsingErrors = GraderConnection.getRegexParsingErrors(regEx, alphabetList)

      // if there are no errors with the inputs, then create a problem object
      if(parsingErrors.isEmpty) {
        val unspecificProblem = createUnspecificProb(shortDescription, longDescription)

        val alphabetToSave = alphabetList.mkString(" ")
        // create RegexpToDFAProblem object
        val specificProblem : RegexpToDFAProblem = RegexpToDFAProblem.create
        specificProblem.problemId(unspecificProblem).regEx(regEx).alphabet(alphabetToSave)
        specificProblem.save

        return JsCmds.RedirectTo("/problems/index")
      } else {
        return JsCmds.JsShowId("submitbutton") & JsCmds.JsShowId("feedbackdisplay") & JsCmds.SetHtml("parsingerror", Text(parsingErrors.mkString("<br/>")))
      }

    }
    val alphabetField = SHtml.text("", value => {}, "id" -> "alphabetfield")
    val regExField = SHtml.text("", value => {}, "id" -> "regexfield")
    val shortDescriptionField = SHtml.text("", value => {}, "id" -> "shortdescfield")
    val longDescriptionField = SHtml.textarea("", value => {}, "cols" -> "80", "rows" -> "5", "id" -> "longdescfield")

    val hideSubmitButton : JsCmd = JsHideId("submitbutton")
    val alphabetFieldValXmlJs : String = "<alphabetfield>' + document.getElementById('alphabetfield').value + '</alphabetfield>"
    val regexFieldValXmlJs : String = "<regexfield>' + document.getElementById('regexfield').value + '</regexfield>"
    val shortdescFieldValXmlJs : String = "<shortdescfield>' + document.getElementById('shortdescfield').value + '</shortdescfield>"
    val longdescFieldValXmlJs : String = "<longdescfield>' + document.getElementById('longdescfield').value + '</longdescfield>"
    val ajaxCall : JsCmd = SHtml.ajaxCall(JsRaw("'<createattempt>" + alphabetFieldValXmlJs + regexFieldValXmlJs + shortdescFieldValXmlJs + longdescFieldValXmlJs + "</createattempt>'"), create(_))

    val checkAlphabetAndSubmit : JsCmd = JsIf(Call("alphabetChecks",Call("parseAlphabetByFieldName", "alphabetfield")), hideSubmitButton & ajaxCall)

    val submitButton : NodeSeq = <button type='button' id='submitbutton' onclick={checkAlphabetAndSubmit}>Submit</button>

    val template : NodeSeq = Templates(List("regexp-to-dfa-problem", "create")) openOr Text("Could not find template /regexp-to-dfa-problem/create")
    Helpers.bind("createform", template,
      "alphabetfield" -> alphabetField,
      "regexfield" -> regExField,
      "shortdescription" -> shortDescriptionField,
      "longdescription" -> longDescriptionField,
      "submit" -> submitButton)
  }

  /** Should produce a NodeSeq that allows the user to edit the problem
   * associated with the given unspecific problem. */
  override def renderEdit : Box[(Problem, () => Nothing) => NodeSeq] = Full(renderEditFunc)

  private def renderEditFunc(problem : Problem, returnFunc : () => Nothing) : NodeSeq = {

    val regexpToDFAProblem = RegexpToDFAProblem.findByGeneralProblem(problem)


    var alphabet : String = regexpToDFAProblem.getAlphabet
    var shortDescription : String = problem.getShortDescription
    var longDescription : String = problem.getLongDescription
    var regex : String = regexpToDFAProblem.getRegex

    def edit(formValues : String) : JsCmd = {
      val formValuesXml = XML.loadString(formValues)
      val regEx = (formValuesXml \ "regexfield").head.text
      val alphabet = (formValuesXml \ "alphabetfield").head.text
      val shortDescription = (formValuesXml \ "shortdescfield").head.text
      val longDescription = (formValuesXml \ "longdescfield").head.text

      //Keep only the chars
      val alphabetList = alphabet.split(" ").filter(_.length()>0);
      val parsingErrors = GraderConnection.getRegexParsingErrors(regEx, alphabetList)

      if(parsingErrors.isEmpty) {

        val alphabetToSave = alphabetList.mkString(" ")
        val specificProblem : RegexpToDFAProblem = RegexpToDFAProblem.create

        problem.setShortDescription(shortDescription).setLongDescription(longDescription).save()
        regexpToDFAProblem.setAlphabet(alphabetToSave).setRegex(regEx).save()
        returnFunc()
      } else {
        return JsCmds.JsShowId("submitbutton") & JsCmds.JsShowId("feedbackdisplay") & JsCmds.SetHtml("parsingerror", Text(parsingErrors.mkString("<br/>")))
      }
    }

    // Remember to remove all newlines from the generated XML by using filter
    val alphabetFieldValXmlJs : String = "<alphabetfield>' + document.getElementById('alphabetfield').value + '</alphabetfield>"
    val regexFieldValXmlJs : String = "<regexfield>' + document.getElementById('regexfield').value + '</regexfield>"
    val shortdescFieldValXmlJs : String = "<shortdescfield>' + document.getElementById('shortdescfield').value + '</shortdescfield>"
    val longdescFieldValXmlJs : String = "<longdescfield>' + document.getElementById('longdescfield').value + '</longdescfield>"

    val alphabetField = SHtml.text(alphabet, alphabet=_, "id" -> "alphabetfield")
    val regExField = SHtml.text(regex, regex=_, "id" -> "regexfield")
    val shortDescriptionField = SHtml.text(shortDescription, shortDescription = _, "id" -> "shortdescfield")
    val longDescriptionField = SHtml.textarea(longDescription, longDescription = _, "cols" -> "80", "rows" -> "5", "id" -> "longdescfield")

    val ajaxCall : JsCmd = SHtml.ajaxCall(JsRaw("'<createattempt>" + alphabetFieldValXmlJs + regexFieldValXmlJs + shortdescFieldValXmlJs + longdescFieldValXmlJs + "</createattempt>'"), edit(_))
    val hideSubmitButton : JsCmd = JsHideId("submitbutton")
    val checkAlphabetAndSubmit : JsCmd = JsIf(Call("alphabetChecks",Call("parseAlphabetByFieldName", "alphabetfield")), hideSubmitButton & ajaxCall)

    val submitButton : NodeSeq = <button type='button' id='submitbutton' onclick={checkAlphabetAndSubmit}>Submit</button>

    val template : NodeSeq = Templates(List("regexp-to-dfa-problem", "edit")) openOr Text("Could not find template /regexp-to-dfa-problem/edit")
    Helpers.bind("editform", template,
      "alphabetfield" -> alphabetField,
      "regexfield" -> regExField,
      "shortdescription" -> shortDescriptionField,
      "longdescription" -> longDescriptionField,
      "submit" -> submitButton)
  }

  /** Should produce a NodeSeq that allows the user a try to solve the problem
   * associated with the given unspecific problem. The function
   * recordSolutionAttempt must be called once for every solution attempt
   * and expects the grade of the attempt (which must be <= maxGrade) and the
   * time the attempt was made. After finishing the solution attempt, the
   * snippet should send the user back to the overview of problems in the
   * set by calling returnToSet */
  override def renderSolve(generalProblem : Problem, maxGrade : Long, lastAttempt : Box[SolutionAttempt],
                           recordSolutionAttempt : (Int, Date) => SolutionAttempt, returnFunc : () => Unit, remainingAttempts: () => Int,
                           bestGrade: () => Int) : NodeSeq = {

    // load the problem from database
    val regexpToDFAProblem = RegexpToDFAProblem.findByGeneralProblem(generalProblem)

    // method that grades student's attempt (via backend connection)
    def grade(attemptDfaDescription: String): JsCmd = {
      // check there are remaining attempts before continuing
      if (remainingAttempts() <= 0) {
        return JsShowId("feedbackdisplay") &
          SetHtml("feedbackdisplay",
            Text("You do not have any attempts left for this problem. Your final grade is " +
              bestGrade().toString + "/" +
              maxGrade.toString + "."))
      }

      // Ask backend to grade attempt
      val attemptDfaXml = XML.loadString(attemptDfaDescription).toString
      val correctRegexDescription = regexpToDFAProblem.getRegex
      val alphabet = regexpToDFAProblem.getAlphabet
      val attemptTime = Calendar.getInstance.getTime()
      val graderResponse = GraderConnection.getRegexToDFAFeedback(correctRegexDescription, attemptDfaXml, alphabet, maxGrade.toInt)

      // RENDER GRADE RESPONSE
      val numericalGrade = graderResponse._1
      val generalAttempt = recordSolutionAttempt(numericalGrade, attemptTime)

      // Only save the specific attempt if we saved the general attempt
      if (generalAttempt != null) {
        DFAConstructionSolutionAttempt.create.solutionAttemptId(generalAttempt).attemptAutomaton(attemptDfaDescription).save
      }

      val setNumericalGrade: JsCmd = SetHtml("grade", Text(graderResponse._1.toString + "/" + maxGrade.toString))
      val setFeedback: JsCmd = SetHtml("feedback", graderResponse._2)
      val showFeedback: JsCmd = JsShowId("feedbackdisplay")

      return setNumericalGrade & setFeedback & showFeedback & JsCmds.JsShowId("submitbutton")
    }

    // RENDERING THE PAGE
    // retrieve objects and store them in these variables
    val problemAlphabet = regexpToDFAProblem.getAlphabet
    val problemRegEx = regexpToDFAProblem.getRegex

    val alphabetJavaScriptArray = "[\"" + problemAlphabet.mkString("\",\"") + "\"]"
    val alphabetScript: NodeSeq = <script type="text/javascript">Editor.canvas.setAlphabet(
      {alphabetJavaScriptArray}
      )</script>

    val problemAlphabetNodeSeq = Text("{" + problemAlphabet.mkString(",") + "}")
    val problemDescriptionNodeSeq = Text(problemRegEx)


    val hideSubmitButton: JsCmd = JsHideId("submitbutton")
    val ajaxCall: JsCmd = SHtml.ajaxCall(JsRaw("Editor.canvas.exportAutomaton()"), grade(_))
    val submitButton: NodeSeq = <button type='button' id='submitbutton' onclick={hideSubmitButton & ajaxCall}>Submit</button>
    val returnLink: NodeSeq = SHtml.link("/courses/show", returnFunc, Text("Return to Course"))

    // *** Crucial Part? ***
    // go to the "regexp-to-dfa-problem" folder and open "solve.html" template
    val template: NodeSeq = Templates(List("regexp-to-dfa-problem", "solve")) openOr Text("Template /regexp-to-dfa-problem/solve not found")
    // map the following tags found in "solve.html" to the variables generated in this snippet
    return SHtml.ajaxForm(Helpers.bind("dfaeditform", template,
      "alphabetscript" -> alphabetScript,
      "alphabettext" -> problemAlphabetNodeSeq,
      "problemdescription" -> problemDescriptionNodeSeq,
      "submitbutton" -> submitButton,
      "returnlink" -> returnLink))
  }

  /** Is called before the given unspecific problem is deleted from the database.
   * This method should delete everything associated with the given unspecific
   * problem from the database */
  override def onDelete( generalProblem : Problem ) : Unit = {

  }
}
