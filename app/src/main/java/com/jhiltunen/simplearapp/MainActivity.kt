package com.jhiltunen.simplearapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.ar.core.HitResult
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.rendering.ViewRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode

class MainActivity : AppCompatActivity() {
    private lateinit var arFrag: ArFragment
    private var viewRenderable: ViewRenderable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        arFrag = supportFragmentManager.findFragmentById(
            R.id.sceneform_fragment
        ) as ArFragment
        ViewRenderable.builder()
            .setView(this, R.layout.rend_text)
            .build()
            .thenAccept { viewRenderable = it }

        arFrag.setOnTapArPlaneListener { hitResult: HitResult?, _, _ ->
            viewRenderable ?: return@setOnTapArPlaneListener
            //Creates a new anchor at the hit location
            val anchor = hitResult!!.createAnchor()
            //Creates a new anchorNode attaching it to anchor
            val anchorNode = AnchorNode(anchor)
            // Add anchorNode as root scene node's child
            anchorNode.parent = arFrag.arSceneView.scene
            // Can be selected, rotated...
            val viewNode = TransformableNode(arFrag.transformationSystem)
            viewNode.renderable = viewRenderable
            // Add viewNode as anchorNode's child
            viewNode.parent = anchorNode
            // Sets this as the selected node in the TransformationSystem
            viewNode.select()
        }

    }
}