package net.enilink.beginner.web.snippet.beginner

import java.util.GregorianCalendar
import net.enilink.beginner.web.util.SnippetHelpers._
import javax.xml.datatype.DatatypeFactory
import net.enilink.komma.em.concepts.IResource
import net.enilink.lift.util.CurrentContext
import net.enilink.lift.util.Globals
import net.liftweb.common.Empty
import net.liftweb.common.Full
import net.liftweb.http.S
import net.liftweb.http.SHtml
import net.liftweb.http.js.JsCmds
import net.liftweb.http.js.JsCmds.Run
import net.liftweb.util.Helpers.strToCssBindPromoter
import net.enilink.komma.core.URIs
import net.enilink.beginner.web.util.DCTERMS

class Experiments {
  def create = SHtml.hidden(() => {
    val result = for {
      model <- Globals.contextModel.vend
      name <- doAlert(paramNotEmpty("name", "Bitte einen Namen eingeben."))
    } yield {
      val em = model.getManager
      val uri = model.getURI.appendLocalPart(name)
      if (em.hasMatch(uri, null, null)) {
        doAlert(failure("name", "Ein Element mit diesem Namen existiert bereits."))
      } else {
        val experiment = em.createNamed(uri, URIs.createURI("example:Experiment")).asInstanceOf[IResource]
        experiment.setRdfsLabel(name)
        S.param("description").filter(!_.isEmpty) foreach {
          desc => experiment.setRdfsComment(desc)
        }
        experiment.set(DCTERMS.PROPERTY_DATE, DatatypeFactory.newInstance.newXMLGregorianCalendar(new GregorianCalendar))
        Full(Run(s"$$(document).trigger('experiment-created', ['$experiment']);"))
      }
    }
    (result openOr Empty) openOr JsCmds.Noop
  })

  def delete = "^ [onclick]" #> {
    val rbox = CurrentContext.value.map { ctx => ctx.subject }
    SHtml.onEvent { s =>
      (for {
        model <- Globals.contextModel.vend
        resource <- rbox
      } yield {
        model.getManager.removeRecursive(resource, true)
        Run(s"""$$('[about="$resource"]').remove()""")
      }) openOr JsCmds.Noop
    }
  }
}