package com.automatatutor.model

//import com.automatatutor.model.DFAMinimisationProblem.{bulkDelete_!!, find}
//import scala.xml.{NodeSeq, XML}
//import net.liftweb.mapper.{By, IdPK, LongKeyedMapper, LongKeyedMetaMapper, MappedLongForeignKey, MappedText}
import net.liftweb.mapper.MappedString
import net.liftweb.mapper.LongKeyedMapper
import net.liftweb.mapper.LongKeyedMetaMapper
import net.liftweb.mapper.MappedLongForeignKey
import net.liftweb.mapper.IdPK
import net.liftweb.mapper.By
import net.liftweb.mapper.MappedText
import scala.xml.XML
import scala.xml.NodeSeq

//Interface for database. handles queries, data persistance etc.

class DFAMinimisationProblem extends LongKeyedMapper[DFAMinimisationProblem] with IdPK with SpecificProblem[DFAMinimisationProblem] {
  def getSingleton = DFAMinimisationProblem

  protected object problemId extends MappedLongForeignKey(this, Problem)
  protected object automaton extends MappedText(this)

  def getGeneralProblem = this.problemId.obj openOrThrowException "Every DFAMinimisationProblem must have a ProblemId"
  override def setGeneralProblem(problem : Problem) : DFAMinimisationProblem = this.problemId(problem)

  def getAutomaton = this.automaton.is
  def setAutomaton(automaton : String) = this.automaton(automaton)
  def setAutomaton(automaton : NodeSeq) = this.automaton(automaton.mkString)

  def getXmlDescription : NodeSeq = XML.loadString(this.automaton.is)

  def getAlphabet : Seq[String] = (getXmlDescription \ "alphabet" \ "symbol").map(_.text)

  override def copy(): DFAMinimisationProblem = {
    val retVal = new DFAMinimisationProblem
    retVal.problemId(this.problemId.get)
    retVal.automaton(this.automaton.get)
    return retVal
  }
}

object DFAMinimisationProblem extends DFAMinimisationProblem with LongKeyedMetaMapper[DFAMinimisationProblem] {
  def findByGeneralProblem(generalProblem : Problem) : DFAMinimisationProblem =
    find(By(DFAMinimisationProblem.problemId, generalProblem)) openOrThrowException("Must only be called if we are sure that generalProblem is a DFAMinimisationProblem")

  def deleteByGeneralProblem(generalProblem : Problem) : Boolean =
    bulkDelete_!!(By(DFAMinimisationProblem.problemId, generalProblem))
}
