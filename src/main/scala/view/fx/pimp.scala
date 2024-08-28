package view.fx

import scalafx.Includes.jfxNode2sfx
import scalafx.scene.layout.Pane

extension (pane: Pane)
  def isAlreadyIn(id: String): Boolean =
    pane.children.exists(_.id.value == id)
