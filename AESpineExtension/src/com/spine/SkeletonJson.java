package com.spine;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

import com.badlogic.gdx.utils.Array;
import com.spine.Animation.AttachmentTimeline;
import com.spine.Animation.CurveTimeline;
import com.spine.Animation.RotateTimeline;
import com.spine.Animation.ScaleTimeline;
import com.spine.Animation.Timeline;
import com.spine.Animation.TranslateTimeline;
import com.spine.AtlasParser.Asset;
import com.spine.attachments.RegionAttachment;
import com.spine.attachments.SpineTextureRegion;
import com.spine.attachments.TextureAtlasAttachmentLoader;

public class SkeletonJson {
	static public final String TIMELINE_SCALE = "scale";
	static public final String TIMELINE_ROTATE = "rotate";
	static public final String TIMELINE_TRANSLATE = "translate";
	static public final String TIMELINE_ATTACHMENT = "attachment";
	static public final String TIMELINE_COLOR = "color";

	private final AttachmentLoader attachmentLoader;
	private float scale = 1;
	
	private LinkedList<SpineTextureRegion> regions = new LinkedList<SpineTextureRegion>();
	
	private AtlasParser atlasParser;

	public SkeletonJson (AtlasParser pAtlasParser, Context context) {
		atlasParser = pAtlasParser;
		attachmentLoader = new TextureAtlasAttachmentLoader(atlasParser.getAtlas(), context);
	}

	public SkeletonJson (AttachmentLoader attachmentLoader) {
		this.attachmentLoader = attachmentLoader;
	}

	public float getScale () {
		return scale;
	}

	/** Scales the bones, images, and animations as they are loaded. */
	public void setScale (float scale) {
		this.scale = scale;
	}
	
	public LinkedList<SpineTextureRegion> getRegions() {
		return regions;
	}
	
	private JSONObject streamToJson(InputStream in) throws IOException, JSONException {
		int size = in.available();
		byte[] buffer = new byte[size];
		in.read(buffer);
		in.close();
		String bufferString = new String(buffer);
		return new JSONObject(bufferString);
	}
	
	private JSONArray getJSONArray(JSONObject json, String pKey) {
		JSONArray array = null;
		try {
			array = json.getJSONArray(pKey);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return array;
	}
	
	public SkeletonData readSkeletonData (InputStream file) {
		if (file == null) throw new IllegalArgumentException("file cannot be null.");
		
		SkeletonData skeletonData = new SkeletonData();
		
		try {
			JSONObject json = streamToJson(file);
			
			// Bones.
			JSONArray oBones = getJSONArray(json, "bones");
			for(int i = 0; i < oBones.length(); i++) {
				JSONObject boneMap = (JSONObject) oBones.get(i);
				BoneData parent = null;
				String parentName = boneMap.has("parent") ? (String) boneMap.get("parent") : null;
				if (parentName != null) {
					parent = skeletonData.findBone(parentName);
					if (parent == null) throw new Exception("Parent bone not found: " + parentName);
				}
				BoneData boneData = new BoneData((String)boneMap.get("name"), parent);
				boneData.length = getFloat(boneMap, "length", 0) * scale;
				boneData.x = getFloat(boneMap, "x", 0) * scale;
				boneData.y = getFloat(boneMap, "y", 0) * scale;
				boneData.rotation = getFloat(boneMap, "rotation", 0);
				boneData.scaleX = getFloat(boneMap, "scaleX", 1);
				boneData.scaleY = getFloat(boneMap, "scaleY", 1);
				skeletonData.addBone(boneData);	
				if (Skeleton.debug) Log.i("BONE","NEW BONE:"+boneData.getName());
			}
			
			// Slots.
			JSONArray slots = json.getJSONArray("slots");
			if (slots != null) {
				for(int i = 0; i < slots.length(); i++) {
					JSONObject slotMap = (JSONObject) slots.get(i);
					String slotName = getString(slotMap, "name", "");
					String boneName = getString(slotMap, "bone", "");
					BoneData boneData = skeletonData.findBone(boneName);
					if (boneData == null) throw new Exception("Slot bone not found: " + boneName);
					SlotData slotData = new SlotData(slotName, boneData);
					// String color = getString(slotMap, "color", "");
					// TODO: Define color
					//if (color != null) slotData.getColor().set(ColorUtils.(color));
					String attachmentName = getString(slotMap, "attachment", null);
					slotData.setAttachmentName(attachmentName);
					if (Skeleton.debug) Log.i("SLOT","loading slot, name:"+attachmentName );
					skeletonData.addSlot(slotData);
				}
			}

			// Skins.
			JSONObject skins = json.getJSONObject("skins");
			if (skins != null) {
				JSONArray keys = skins.names();
				for(int i=0; i < keys.length(); i++) {
					String key = (String) keys.get(i);
					JSONObject skinDatas = (JSONObject) skins.getJSONObject(key);
					Skin skin = new Skin(key);
					JSONArray pSkinNames = skinDatas.names();
					for(int j=0; j < pSkinNames.length(); j++)
					{
						String akey = pSkinNames.getString(j);
						int slotIndex = skeletonData.findSlotIndex(akey);	
						JSONObject data = skinDatas.getJSONObject(akey);	
						//change list of eyes childrens fron json into arraylist 
						ArrayList<String> list = new ArrayList<String>();     
						JSONArray jsonArray = (JSONArray)data.names(); 
						if (jsonArray != null) { 
						   int len = jsonArray.length();
						   for (int c = 0;c < len;c++){ 
							   list.add(jsonArray.get(c).toString());	
						   } 
						} 
						for (String pSubSkinKey : list) {
							Attachment attachment = readAttachment(pSubSkinKey, data);
							if (attachment != null) skin.addAttachment(slotIndex, pSubSkinKey, attachment);	
							if (Skeleton.debug) Log.i("SKIN","load att:"+attachment+" (subkey:"+pSubSkinKey+"), akey:"+akey+" | SKIN NAME:"+key+", REGION:"+attachment);
						}
					}
					skeletonData.addSkin(skin);
					if (skin.name.equals("default")) skeletonData.setDefaultSkin(skin);
				}
			}
			
			// Animations
			JSONObject animations = json.getJSONObject("animations");
			if (animations != null) {
				JSONArray keys = animations.names();	
				for(int i=0; i < keys.length(); i++) {
					String animationName = (String) keys.get(i);
					JSONObject animationDatas = (JSONObject) animations.getJSONObject(animationName);
					if (Skeleton.debug) Log.i("Animation","loading animation name:"+animationName);
					readAnimation(animationDatas, skeletonData, animationName);
				}
			}
			
		} catch(Exception e) {
			e.printStackTrace();
		}
		skeletonData.mBonesData.shrink();
		skeletonData.mSlotsData.shrink();
		skeletonData.mSkins.shrink();
		return skeletonData;
	}

	private Attachment readAttachment (String name, JSONObject map){
		Attachment attachment = null;
		try {
			AttachmentType type = AttachmentType.region; //AttachmentType.valueOf((String) map.get(AttachmentType.region.name()));
			attachment = attachmentLoader.newAttachment(type, name);

			if (attachment instanceof RegionAttachment) {
				RegionAttachment regionAttachment = (RegionAttachment) attachment;
				//there are more entries in map eyes[eyes,eyes-closed]
				String regionName = name;
				JSONObject v = map.getJSONObject(regionName);
				regionAttachment.setX(getFloat(v, "x", 0) * scale);
				regionAttachment.setY(getFloat(v, "y", 0) * scale);
				regionAttachment.setScaleX(getFloat(v, "scaleX", 1));
				regionAttachment.setScaleY(getFloat(v, "scaleY", 1));
				regionAttachment.setRotation(getFloat(v, "rotation", 0));
				regionAttachment.setWidth(getFloat(v, "width", 32) * scale);
				regionAttachment.setHeight(getFloat(v, "height", 32) * scale);
				regionAttachment.updateOffset();
				SpineTextureRegion region = atlasParser.getRegion(regionName);
				Asset pAsset = atlasParser.assets.get(regionName);
				if (pAsset==null) {
//					throw new Exception("asset is null for region:"+regionName);
				}
				//region.originalWidth
				region.originalWidth = (int) pAsset.width;
				region.originalHeight = (int) pAsset.height;
				region.packedWidth = region.originalWidth;
				region.packedHeight = region.originalHeight;
				region.offsetX = pAsset.offsetX;
				region.offsetY = pAsset.offsetY;
				((RegionAttachment)attachment).setRegion(region);//		inside-> 		regionAttachment.updateOffset();
				regions.add(region);
				if (Skeleton.debug) Log.i("REGIONS","set region:"+region+" | name:"+regionName);
			}
		
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return attachment;
	}

	private float getFloat (JSONObject map, String name, float defaultValue) throws JSONException {
		String value = map.has(name) ? map.getString(name) : null;
		if (value == null) return defaultValue;
		return Float.parseFloat(value);
	}
	
	private String getString (JSONObject map, String name, String defaultValue) throws JSONException {
		String value = map.has(name) ? map.getString(name) : null;
		if (value == null) return defaultValue;
		return value;
	}

	
	public void readAnimation (final JSONObject pJSONAnimationObject, SkeletonData skeletonData,String pAnimationName) {
		if (skeletonData == null) throw new IllegalArgumentException("skeletonData cannot be null.");
		try {
			Array<Timeline> timelines = new Array<Timeline>();
			float duration = 0;
			JSONObject pBonesInformation = pJSONAnimationObject.getJSONObject("bones");
			final JSONArray pval = pBonesInformation.names();
			for(int i = 0; i < pval.length(); i++) {
				String boneName = pval.getString(i);
				JSONObject pData = (JSONObject) pBonesInformation.get(boneName);
				int pBoneIndex = skeletonData.findBoneIndex(boneName);
				JSONArray pAnimationNames = pData.names();
				for(int j=0; j < pAnimationNames.length(); j++){
					String timelineName = pAnimationNames.getString(j);
					JSONArray pAnimData = pData.getJSONArray(timelineName);
					if (timelineName.equals(TIMELINE_ROTATE)) {
						RotateTimeline timeline = new RotateTimeline(pAnimData.length());
						timeline.setBoneIndex(pBoneIndex);	
						int keyframeIndex = 0;
						for (int g=0; g < pAnimData.length(); g++) {
							JSONObject valueMap = (JSONObject) pAnimData.get(g);
							float time = getFloat(valueMap, "time", 0);
							float angle = getFloat(valueMap, "angle", 0);
							timeline.setKeyframe(keyframeIndex, time, angle);
							readCurve(timeline, keyframeIndex, valueMap);
							if (Skeleton.debug) Log.i("TIMELINE","new timeline keyframe index:"+keyframeIndex+", time:"+time+",angle:"+angle+",name:"+timelineName+",b:"+boneName);
							keyframeIndex++;
						}
						timelines.add(timeline);
						float timelineDuration = timeline.getKeyframes()[timeline.getKeyframeCount() * 2 - 2];
						duration = Math.max(duration, timelineDuration);

					} else if (timelineName.equals(TIMELINE_TRANSLATE) || timelineName.equals(TIMELINE_SCALE)) {
						TranslateTimeline timeline;
						float timelineScale = 1;
						if (timelineName.equals(TIMELINE_SCALE))
							timeline = new ScaleTimeline(pAnimData.length());
						else {
							timeline = new TranslateTimeline(pAnimData.length());
							timelineScale = scale;
						}
						timeline.setBoneIndex(pBoneIndex);
						int keyframeIndex = 0;
						for (int g=0; g < pAnimData.length(); g++) {
							JSONObject valueMap = (JSONObject) pAnimData.get(g);
							float time = getFloat(valueMap, "time", 0);
							Float x = getFloat(valueMap, "x", 0), y = getFloat(valueMap, "y", 0);
							timeline.setKeyframe(keyframeIndex, time, x == null ? 0 : (x * timelineScale), y == null ? 0
								: (y * timelineScale));
							readCurve(timeline, keyframeIndex, valueMap);
							keyframeIndex++;
						}
						timelines.add(timeline);
						float timelineDuration = timeline.getKeyframes()[timeline.getKeyframeCount() * 3 - 3];
						duration = Math.max(duration, timelineDuration);

					} else {
						throw new RuntimeException("Invalid timeline type for a bone: " + timelineName + " (" + boneName + ")");
					}
				}
			}
			
			 
			if (pJSONAnimationObject.has("slots")){
			JSONObject slotsMap = pJSONAnimationObject.getJSONObject("slots");
			if (slotsMap != null) {
				final JSONArray pSlotsNames = slotsMap.names();
				for(int i = 0; i < pSlotsNames.length(); i++) {
					String slotName = pSlotsNames.getString(i);
					JSONObject pSlotData = (JSONObject) slotsMap.get(slotName);
					int slotIndex = skeletonData.findSlotIndex(slotName);
					if (Skeleton.debug) Log.i("ANSLOT","loading animation slot:"+slotName+", index:"+slotIndex);
					JSONArray pSlotAttachments = pSlotData.names();
					for(int j=0; j < pSlotAttachments.length(); j++){
						String pAttachmentKey = pSlotAttachments.getString(j);
						JSONArray pTimelines = pSlotData.getJSONArray(pAttachmentKey);
						if (Skeleton.debug) Log.i("ANSLOT","loading attachment slot:"+slotName+", index:"+slotIndex+", name:"+pAttachmentKey);
						if (pAttachmentKey.equals(TIMELINE_ATTACHMENT)) {
							AttachmentTimeline timeline = new AttachmentTimeline(pTimelines.length());	
							timeline.setSlotIndex(slotIndex);
							for (int keyFrameIndex = 0;keyFrameIndex<pTimelines.length();keyFrameIndex++){
								JSONObject pTimeLineData = pTimelines.getJSONObject(keyFrameIndex);	
									float 	dataTime = getFloat(pTimeLineData, "time", 0f);	
									String 	dataName = getString(pTimeLineData, "name", "error");
									timeline.setKeyframe(keyFrameIndex, dataTime, dataName);
									if (Skeleton.debug) Log.i("ANSLOT","loading timeline: time="+dataTime+", name="+dataName+",slotname:"+slotName);
								}
							timelines.add(timeline);
							//increase duration if needed
							duration = Math.max(duration, timeline.getKeyframes()[timeline.getKeyframeCount() - 1]);
						}
						//TODO , color attachment 
						
						
					}		
//					OrderedMap<?, ?> timelineMap = (OrderedMap)entry.value;
//					for (Entry timelineEntry : timelineMap.entries()) {
//						Array<OrderedMap> values = (Array)timelineEntry.value;
//						String timelineName = (String)timelineEntry.key;
//						if (timelineName.equals(TIMELINE_COLOR)) {
//							ColorTimeline timeline = new ColorTimeline(values.size);
//							timeline.setSlotIndex(slotIndex);
//							int keyframeIndex = 0;
//							for (OrderedMap valueMap : values) {
//								float time = (Float)valueMap.get("time");
////								Color color = Color.valueOf((String)valueMap.get("color"));
////								timeline.setFrame(keyframeIndex, time, color.r, color.g, color.b, color.a);
////								readCurve(timeline, keyframeIndex, valueMap);
//								keyframeIndex++;
//							}
//							timelines.add(timeline);
////							duration = Math.max(duration, timeline.getFrames()[timeline.getFrameCount() * 5 - 5]);
//						} else if (timelineName.equals(TIMELINE_ATTACHMENT)) {
//							AttachmentTimeline timeline = new AttachmentTimeline(values.size);
//							timeline.setSlotIndex(slotIndex);
//							int keyframeIndex = 0;
//							for (OrderedMap valueMap : values) {
//								float time = (Float)valueMap.get("time");
////								timeline.setFrame(keyframeIndex++, time, (String)valueMap.get("name"));
//							}
//							timelines.add(timeline);
////							duration = Math.max(duration, timeline.getFrames()[timeline.getFrameCount() - 1]);
//						} else
//							throw new RuntimeException("Invalid timeline type for a slot: " + timelineName + " (" + slotName + ")");
//					}
				}
			}
			}
			
			
			timelines.shrink();
			skeletonData.addAnimation(new Animation(pAnimationName, timelines, duration));
		} catch (JSONException e) {
			e.printStackTrace();
		}  
	}
	
	
	@Deprecated
	public Animation readAnimation (InputStream file, SkeletonData skeletonData,String pAnimationName) {
		if (file == null) throw new IllegalArgumentException("file cannot be null.");
		if (skeletonData == null) throw new IllegalArgumentException("skeletonData cannot be null.");
		Animation anim = null;
		try {
			JSONObject map = streamToJson(file);
			Array<Timeline> timelines = new Array<Timeline>();
			float duration = 0;
			map = map.getJSONObject("bones");
			final JSONArray pval = map.names();
			for(int i = 0; i < pval.length(); i++) {
				String boneName = pval.getString(i);
				JSONObject pData = (JSONObject) map.get(boneName);
				int pBoneIndex = skeletonData.findBoneIndex(boneName);
				JSONArray pAnim = pData.names();
				for(int j=0; j < pAnim.length(); j++){
					String timelineName = pAnim.getString(j);
					JSONArray pAnimData = pData.getJSONArray(timelineName);
					if (timelineName.equals(TIMELINE_ROTATE)) {
						RotateTimeline timeline = new RotateTimeline(pAnimData.length());
						timeline.setBoneIndex(pBoneIndex);	
						int keyframeIndex = 0;
						for (int g=0; g < pAnimData.length(); g++) {
							JSONObject valueMap = (JSONObject) pAnimData.get(g);
							float time = getFloat(valueMap, "time", 0);
							float angle = getFloat(valueMap, "angle", 0);
							timeline.setKeyframe(keyframeIndex, time, angle);
							readCurve(timeline, keyframeIndex, valueMap);
							keyframeIndex++;
						}
						timelines.add(timeline);
						float timelineDuration = timeline.getKeyframes()[timeline.getKeyframeCount() * 2 - 2];
						duration = Math.max(duration, timelineDuration);
					} else if (timelineName.equals(TIMELINE_TRANSLATE) || timelineName.equals(TIMELINE_SCALE)) {
						TranslateTimeline timeline;
						float timelineScale = 1;
						if (timelineName.equals(TIMELINE_SCALE))
							timeline = new ScaleTimeline(pAnimData.length());
						else {
							timeline = new TranslateTimeline(pAnimData.length());
							timelineScale = scale;
						}
						timeline.setBoneIndex(pBoneIndex);
						int keyframeIndex = 0;
						for (int g=0; g < pAnimData.length(); g++) {
							JSONObject valueMap = (JSONObject) pAnimData.get(g);
							float time = getFloat(valueMap, "time", 0);
							Float x = getFloat(valueMap, "x", 0), y = getFloat(valueMap, "y", 0);
							timeline.setKeyframe(keyframeIndex, time, x == null ? 0 : (x * timelineScale), y == null ? 0
								: (y * timelineScale));
							readCurve(timeline, keyframeIndex, valueMap);
							keyframeIndex++;
						}
						timelines.add(timeline);
						float timelineDuration = timeline.getKeyframes()[timeline.getKeyframeCount() * 3 - 3];
						duration = Math.max(duration, timelineDuration);
					} else {
						throw new RuntimeException("Invalid timeline type for a bone: " + timelineName + " (" + boneName + ")");
					}
				}
			}
			
			timelines.shrink();
			anim = new Animation(pAnimationName,timelines, duration);
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return anim;
	}

	private void readCurve (CurveTimeline timeline, int keyframeIndex, JSONObject valueMap) throws JSONException {
		Object curveObject = valueMap.has("curve") ? valueMap.get("curve") : null;
		if (curveObject == null) return;
		if (curveObject.equals("stepped"))
			timeline.setStepped(keyframeIndex);
		else if (curveObject instanceof JSONArray) {
			JSONArray curve = (JSONArray) curveObject;
			float c0 = Float.parseFloat(curve.getString(0));
			float c1 = Float.parseFloat(curve.getString(1));
			float c2 = Float.parseFloat(curve.getString(2));
			float c3 = Float.parseFloat(curve.getString(3));
			timeline.setCurve(keyframeIndex, c0, c1, c2, c3);
		}
	}
}
