package com.automatatutor.model

import com.automatatutor.model.RegexpToDFAProblem.find
import net.liftweb.mapper.{By, IdPK, KeyedMetaMapper, LongKeyedMapper, LongKeyedMetaMapper, MappedLongForeignKey, MappedText}

import scala.xml.{NodeSeq, XML}

class RegexpToDFAProblem extends LongKeyedMapper[RegexpToDFAProblem] with IdPK with SpecificProblem[RegexpToDFAProblem]{
  def getSingleton = RegexpToDFAProblem

  object problemId extends MappedLongForeignKey(this, Problem)
  object regEx extends MappedText(this)
  object alphabet extends MappedText(this)
  //protected object automaton extends MappedText(this)

  def getAlphabet = this.alphabet.is
  def getRegex = this.regEx.is

  def setRegex(regEx : String) = this.regEx(regEx)
  def setAlphabet(alphabet : String) = this.alphabet(alphabet)


  //def getXmlDescription : NodeSeq = XML.loadString(this.automaton.is)

  override def copy(): RegexpToDFAProblem = {
    val retVal = new RegexpToDFAProblem
    retVal.problemId(this.problemId.get)
    retVal.regEx(this.regEx.get)
    retVal.alphabet(this.alphabet.get)
    return retVal
  }

  override def setGeneralProblem(newProblem: Problem) = this.problemId(newProblem)

}
                                                          //database mapper (kinda thing)
object RegexpToDFAProblem extends RegexpToDFAProblem with LongKeyedMetaMapper[RegexpToDFAProblem] {
  def findByGeneralProblem(generalProblem : Problem) : RegexpToDFAProblem =
    find(By(RegexpToDFAProblem.problemId, generalProblem)) openOrThrowException("Must only be called if we are sure that generalProblem is a RegexpToDFAProblem")
}
