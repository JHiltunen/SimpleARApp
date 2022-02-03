package com.jhiltunen.simplearapp

import android.graphics.Point
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.ar.core.HitResult
import com.google.ar.core.Plane
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.ViewRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import kotlin.math.absoluteValue

class MainActivity : AppCompatActivity() {
    private lateinit var arFrag: ArFragment
    private var viewRenderable: ViewRenderable? = null
    private var modelRenderable: ModelRenderable? = null
    private var TAG = "DBG"
    private lateinit var button: Button

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

        button = findViewById(R.id.btn_add_tree)
        button.setOnClickListener {
            add3dObject()
        }

        // (CC BY 4.0) Donated by Cesium for glTF testing.
        // https://github.com/KhronosGroup/glTF-Sample-Models/
        // put gltf, bin, jpg in assets

        ModelRenderable.builder()
            .setSource(this, Uri.parse("demo.gltf"))
            .setIsFilamentGltf(true)
            .setAsyncLoadEnabled(true)
            .setRegistryId("Avocado")
            .build()
            .thenAccept { modelRenderable = it }
            .exceptionally {
                Log.e(TAG, "something went wrong ${it.localizedMessage}")
                null
            }
    }

    private fun getScreenCenter(): Point {
        // find the root view of the activity
        val vw = findViewById<View>(android.R.id.content)
        // returns center of the screen as a Point object
        return Point(vw.width / 2, vw.height / 2)
    }

    private fun add3dObject() {
        val frame = arFrag.arSceneView.arFrame
        if (frame != null && modelRenderable != null) {
            val pt = getScreenCenter()
            // get list of HitResult of the given location in the camera view
            val hits = frame.hitTest(pt.x.toFloat(), pt.y.toFloat())
            for (hit in hits) {
                val trackable = hit.trackable
                if (trackable is Plane) {
                    val anchorNode = AnchorNode(hit!!.createAnchor())
                    anchorNode.parent = arFrag.arSceneView.scene
                    val mNode = TransformableNode(arFrag.transformationSystem)
                    mNode.renderable = modelRenderable
                    // Default min is 0.75, default max is 1.75.
                    mNode.scaleController.minScale = 0.05f
                    mNode.scaleController.maxScale = 2.0f
                    mNode.localScale = Vector3(0.2f, 0.2f, 0.2f) // scale at 20%
                    mNode.setOnTapListener { _, _ ->
                        button.visibility = View.INVISIBLE
                    }
                    // scale must be set before setting parent
                    mNode.parent = anchorNode
                    mNode.select()
                    break
                }
            }
        }
    }
}