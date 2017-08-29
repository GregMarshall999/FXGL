/*
 * FXGL - JavaFX Game Library. The MIT License (MIT).
 * Copyright (c) AlmasB (almaslvl@gmail.com).
 * See LICENSE for details.
 */

package com.almasb.fxgl.service.impl.display

import com.almasb.fxgl.app.FXGL
import com.almasb.fxgl.service.DialogFactory
import com.almasb.fxgl.ui.FXGLButton
import com.google.inject.Inject
import javafx.beans.property.DoubleProperty
import javafx.geometry.Point2D
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.ProgressIndicator
import javafx.scene.control.TextField
import javafx.scene.layout.HBox
import javafx.scene.layout.Pane
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import javafx.scene.text.Text
import java.util.function.Consumer
import java.util.function.Predicate

/**
 *
 *
 * @author Almas Baimagambetov (almaslvl@gmail.com)
 */
class FXGLDialogFactory
@Inject
private constructor(): DialogFactory {

    private fun createMessage(message: String): Text {
        return FXGL.getUIFactory().newText(message)
    }

    /**
     * Creates a rectangular wrapper around the content node.
     */
    private fun wrap(n: Node): Pane {
        val size = n.userData as Point2D

        val box = Rectangle()
        with(box) {
            width = maxOf(size.x + 200, 600.0)
            height = size.y + 100
            translateY = 3.0
            stroke = Color.AZURE
        }

        return StackPane(box, n)
    }

    override fun messageDialog(message: String, callback: Runnable): Pane {
        val text = createMessage(message)

        val btnOK = FXGL.getUIFactory().newButton("OK")
        btnOK.setOnAction {
            callback.run()
        }

        val vbox = VBox(50.0, text, btnOK)
        vbox.setAlignment(Pos.CENTER)
        vbox.setUserData(Point2D(Math.max(text.layoutBounds.width, 200.0), text.layoutBounds.height * 2 + 50))

        return wrap(vbox)
    }

    override fun confirmationDialog(message: String, callback: Consumer<Boolean>): Pane {
        val text = createMessage(message)

        val btnYes = FXGLButton("YES")
        btnYes.setOnAction {
            callback.accept(true)
        }

        val btnNo = FXGLButton("NO")
        btnNo.setOnAction {
            callback.accept(false)
        }

        val hbox = HBox(btnYes, btnNo)
        hbox.alignment = Pos.CENTER

        val vbox = VBox(50.0, text, hbox)
        vbox.setAlignment(Pos.CENTER)
        vbox.setUserData(Point2D(Math.max(text.layoutBounds.width, 400.0), text.layoutBounds.height * 2 + 50))

        return wrap(vbox)
    }

    override fun inputDialog(message: String, filter: Predicate<String>, callback: Consumer<String>): Pane {
        val text = createMessage(message)

        val field = TextField()
        field.maxWidth = Math.max(text.layoutBounds.width, 200.0)
        field.font = FXGL.getUIFactory().newFont(18.0)

        val btnOK = FXGLButton("OK")

        field.textProperty().addListener { _, _, newInput ->
            btnOK.isDisable = newInput.isEmpty() || !filter.test(newInput)
        }

        btnOK.isDisable = true
        btnOK.setOnAction {
            callback.accept(field.text)
        }

        val vbox = VBox(50.0, text, field, btnOK)
        vbox.setAlignment(Pos.CENTER)
        vbox.setUserData(Point2D(Math.max(text.layoutBounds.width, 200.0), text.layoutBounds.height * 3 + 50 * 2))

        return wrap(vbox)
    }

    override fun inputDialogWithCancel(message: String, filter: Predicate<String>, callback: Consumer<String>): Pane {
        val text = createMessage(message)

        val field = TextField()
        field.maxWidth = Math.max(text.layoutBounds.width, 200.0)
        field.font = FXGL.getUIFactory().newFont(18.0)

        val btnOK = FXGL.getUIFactory().newButton("OK")

        field.textProperty().addListener {
            _, _, newInput -> btnOK.isDisable = newInput.isEmpty() || !filter.test(newInput)
        }

        btnOK.isDisable = true
        btnOK.setOnAction {
            callback.accept(field.text)
        }

        val btnCancel = FXGL.getUIFactory().newButton("CANCEL")
        btnCancel.setOnAction {
            callback.accept("")
        }

        val hBox = HBox(btnOK, btnCancel)
        hBox.alignment = Pos.CENTER

        val vbox = VBox(50.0, text, field, hBox)
        vbox.setAlignment(Pos.CENTER)
        vbox.setUserData(Point2D(Math.max(text.layoutBounds.width, 200.0), text.layoutBounds.height * 3 + 50 * 2))

        return wrap(vbox)
    }

    override fun errorDialog(error: Throwable, callback: Runnable): Pane {
        val text = createMessage(error.toString())

        val btnOK = FXGLButton("OK")
        btnOK.setOnAction {
            callback.run()
        }

        val btnLog = FXGLButton("LOG")
        btnLog.setOnAction {
//            val sw = StringWriter()
//            val pw = PrintWriter(sw)
//            error.printStackTrace(pw)
//            pw.close()
//
//            try {
//                Files.write(Paths.get("LastException.log"), Arrays.asList(*sw.toString().split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()))
//                DialogSubState.showMessageBox("Log has been saved as LastException.log")
//            } catch (ex: Exception) {
//                DialogSubState.showMessageBox("Failed to save log file")
//            }

            callback.run()
        }

        val hbox = HBox(btnOK, btnLog)
        hbox.alignment = Pos.CENTER

        val vbox = VBox(50.0, text, hbox)
        vbox.setAlignment(Pos.CENTER)
        vbox.setUserData(Point2D(Math.max(text.layoutBounds.width, 400.0), text.layoutBounds.height * 2 + 50))

        return wrap(vbox)
    }

    override fun progressDialog(observable: DoubleProperty, callback: Runnable): Pane {
        TODO()
    }

    override fun progressDialogIndeterminate(message: String, callback: Runnable): Pane {
        val progress = ProgressIndicator()
        progress.setPrefSize(200.0, 200.0)

        val btn = Button()
        btn.isVisible = false

        return customDialog(message, progress, callback, btn)
    }

    override fun customDialog(message: String, content: Node, callback: Runnable, vararg buttons: Button): Pane {
        for (btn in buttons) {
            val handler = btn.onAction

            btn.setOnAction { e ->
                callback.run()

                handler?.handle(e)
            }
        }

        val text = createMessage(message)

        val hbox = HBox(*buttons)
        hbox.alignment = Pos.CENTER

        val vbox = VBox(50.0, text, content, hbox)
        vbox.setAlignment(Pos.CENTER)

        // TODO: find a way to properly compute size
        vbox.setUserData(Point2D(Math.max(text.layoutBounds.width, 200.0),
                text.layoutBounds.height * 3 + 50 * 2))

        return wrap(vbox)
    }
}