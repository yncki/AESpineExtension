package com;

import com.spine.Animation;
import com.spine.AnimationState;
import com.spine.AnimationStateData;
import com.spine.AtlasParser;
import com.spine.Bone;
import com.spine.Skeleton;
import com.spine.SkeletonData;
import com.spine.SkeletonJson;

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
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.ui.activity.SimpleBaseGameActivity;
import org.andengine.util.adt.color.Color;

import java.io.IOException;

public class SpineTestActivity extends SimpleBaseGameActivity implements IUpdateHandler {

	private Camera mCamera;
	private Bone root;
	private Animation animationBieg;
	private SkeletonData skeletonData;
	private SkeletonJson json;
	private AtlasParser mSkeletonAtlasParser;
	private SpriteBatch mSkeletonBatch;
	private Scene pScene;
	private AnimationState state;
	private Skeleton skeleton;
	private Animation animationLot;
	private Animation animationSpadanie;
	
	
	
	public static VertexBufferObjectManager mVBO;

	public EngineOptions onCreateEngineOptions() {
		mCamera = new Camera(0, 0, 1280, 800);
		EngineOptions mEngineOptions = new EngineOptions(true, ScreenOrientation.LANDSCAPE_FIXED,new FillResolutionPolicy(), mCamera);
        return mEngineOptions;
	}

	@Override
	protected void onCreateResources()
	{
	    mVBO = this.getVertexBufferObjectManager();
		try {
			Skeleton.debug = true;
			mSkeletonAtlasParser = new AtlasParser(this.getAssets().open("santa.txt"), this.getTextureManager(),1024,256, this,"",TextureOptions.NEAREST);
			json = new SkeletonJson(mSkeletonAtlasParser, this);
			skeletonData = json.readSkeletonData(this.getAssets().open("santa.json"));
			
			animationBieg = skeletonData.findAnimation("run"); 
			animationLot = skeletonData.findAnimation("die");
			animationSpadanie = skeletonData.findAnimation("die");
			
			// Define mixing between animations.
			AnimationStateData stateData = new AnimationStateData(skeletonData);
			stateData.setMix(animationLot, animationBieg, 0.2f);
			stateData.setMix(animationLot, animationSpadanie, 0.4f);
			stateData.setMix(animationSpadanie, animationBieg, 0.2f);
			stateData.setMix(animationBieg, animationLot, 0.2f);
			
			state = new AnimationState(stateData);
			state.setAnimation(animationLot, true);
			skeleton = new Skeleton(skeletonData);	
			skeleton.setSkin("lato");
			skeleton.setToBindPose();
			root = skeleton.getRootBone();
			root.setX(150);
			root.setY(50);
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
		mSkeletonBatch = new SpriteBatch(mSkeletonAtlasParser.getAtlas(), 100, this.getVertexBufferObjectManager()); //max 100 elements to draw 
		mSkeletonBatch.submit();
		pScene.attachChild(mSkeletonBatch);
		createDebug(pScene);
		//root.setX(50);
		//skeleton.setBonesToBindPose();
		//animation.apply(skeleton, 1f, true);
		
		
		Rectangle R = new Rectangle(600, 400, 80, 80, getVertexBufferObjectManager()){
			@Override
			public boolean onAreaTouched(org.andengine.input.touch.TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY) {
				if (pSceneTouchEvent.isActionDown()) state.setAnimation(animationBieg, true);
				animationBieg.baseSpeed = 450;
				return true;
			}
		};
		Rectangle R2 = new Rectangle(700, 400, 80, 80, getVertexBufferObjectManager()){
			@Override
			public boolean onAreaTouched(org.andengine.input.touch.TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY) {
				if (pSceneTouchEvent.isActionDown()) state.setAnimation(animationLot, true);
				return true;
			};
		};
		Rectangle R3 = new Rectangle(800, 400, 80, 80, getVertexBufferObjectManager()){
			@Override
			public boolean onAreaTouched(org.andengine.input.touch.TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY) {
				if (pSceneTouchEvent.isActionDown()) state.setAnimation(animationSpadanie, true);
				return true;
			};
		};
		
		pScene.registerTouchArea(R);
		pScene.registerTouchArea(R2);
		
		pScene.registerTouchArea(R3);
		
		pScene.attachChild(R3);
		pScene.attachChild(R2);
		pScene.attachChild(R);
		
		return pScene;
	}
	
	public void onUpdate(float pSecondsElapsed) {
		
//		if (state.getAnimation()==animationBieg) pSecondsElapsed*=2;
		state.update(pSecondsElapsed,1200); //current input velocity
		state.apply(skeleton);
		
//		if (state.getAnimation().getName().equals("biegg")) { //change "walk" to static predefined animation name to 
//			// After one second, change the current animation. Mixing is done by AnimationState for you.
//			if (state.getTime() > 10) state.setAnimation("wybicie", false);
//		} else {
//			if (state.getTime() > 1) state.setAnimation("biegg", true);
//		}
//		state.setAnimation(animationBieg, true);
		skeleton.updateWorldTransform();	
		skeleton.draw(mSkeletonBatch);
		skeleton.update(pSecondsElapsed);
		mSkeletonBatch.submit();
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
