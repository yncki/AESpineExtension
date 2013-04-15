package com;

import java.io.IOException;

import org.andengine.engine.camera.Camera;
import org.andengine.engine.handler.IUpdateHandler;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.FillResolutionPolicy;
import org.andengine.entity.Entity;
import org.andengine.entity.primitive.Line;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.Background;
import org.andengine.entity.sprite.batch.SpriteBatch;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.ui.activity.SimpleBaseGameActivity;
import org.andengine.util.adt.color.Color;

import com.spine.Animation;
import com.spine.AnimationState;
import com.spine.AnimationStateData;
import com.spine.AtlasParser;
import com.spine.Bone;
import com.spine.Skeleton;
import com.spine.SkeletonData;
import com.spine.SkeletonJson;

public class TestActivity extends SimpleBaseGameActivity implements IUpdateHandler {

	private Camera mCamera;
	private Bone root;
	private Animation animationWALK;
	private SkeletonData skeletonData;
	private SkeletonJson json;
	private AtlasParser atlasParser;
	private SpriteBatch batch;
	private Scene pScene;
	private Animation animationJUMP;
	private AnimationState state;
	private Skeleton skeleton;
	
	
	
	public static VertexBufferObjectManager vertex;

	public EngineOptions onCreateEngineOptions() {
		mCamera = new Camera(0, 0, 800, 480);
		EngineOptions mEngineOptions = new EngineOptions(true, ScreenOrientation.LANDSCAPE_FIXED,new FillResolutionPolicy(), mCamera);
        return mEngineOptions;
	}

	@Override
	protected void onCreateResources()
	{
	    vertex = this.getVertexBufferObjectManager();
		try {
			Skeleton.debug = false;
			atlasParser = new AtlasParser(this.getAssets().open("spineboy.atlas"), this.getTextureManager(),256,256, this);
			json = new SkeletonJson(atlasParser, this);
			skeletonData = json.readSkeletonData(this.getAssets().open("spineboy.json"));
			animationWALK = skeletonData.findAnimation("walk"); 
			animationJUMP = skeletonData.findAnimation("jump");
			
			
			// Define mixing between animations.
			AnimationStateData stateData = new AnimationStateData(skeletonData);
//			stateData.setMix("walk", "jump", 0.4f);
			stateData.setMix(animationWALK, animationJUMP, 0.4f);
			stateData.setMix(animationJUMP, animationWALK, 0.4f);

			state = new AnimationState(stateData);
			state.setAnimation("walk", true);
			
			skeleton = new Skeleton(skeletonData);	
			skeleton.setToBindPose();
			root = skeleton.getRootBone();
			root.setX(150);
			root.setY(050);
			root.setScaleX(1f);
			root.setScaleY(1f);
			skeleton.updateWorldTransform();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected Scene onCreateScene(){
		pScene = new Scene();
		pScene.registerUpdateHandler(this);
		pScene.setBackground(new Background(Color.BLACK));
		batch = new SpriteBatch(atlasParser.getAtlas(), 100, this.getVertexBufferObjectManager()); //max 100 elements to draw 
		batch.submit();
		pScene.attachChild(batch);
		
		createDebug(pScene);
		
		//root.setX(50);
		//skeleton.setBonesToBindPose();
		//animation.apply(skeleton, 1f, true);
		return pScene;
	}
	
	public void onUpdate(float pSecondsElapsed) {
		state.update(pSecondsElapsed);
		state.apply(skeleton);
		if (state.getAnimation().getName().equals("walk")) { //change "walk" to static predefined animation name to 
			// After one second, change the current animation. Mixing is done by AnimationState for you.
			if (state.getTime() > 10) state.setAnimation("jump", false);
		} else {
			if (state.getTime() > 1) state.setAnimation("walk", true);
		}
		skeleton.updateWorldTransform();	
		skeleton.draw(batch);
		skeleton.update(pSecondsElapsed);
		batch.submit();
	}

	public void reset() {
		
	}
	
	public void createDebug(Entity pParent){
		if (Skeleton.debug){
			for (Bone pBone : skeleton.getBones()) {	
				pBone.mDebugLine = new Line(0, 0, 0, 0, this.getVertexBufferObjectManager());
				pParent.attachChild(pBone.mDebugLine);
				pBone.mDebugLine.setColor(1, 1, 1);
				pBone.mDebugRoot = new Rectangle(0,0,1,1,this.getVertexBufferObjectManager());
				pParent.attachChild(pBone.mDebugRoot);
				pBone.mDebugRoot.setColor(1, 1, 0);	
			}
		}
	}
	
}
