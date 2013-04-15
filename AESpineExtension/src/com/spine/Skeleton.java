package com.spine;

import org.andengine.entity.sprite.batch.SpriteBatch;
import org.andengine.util.adt.color.Color;

import com.badlogic.gdx.utils.Array;

public class Skeleton {
	
	public static boolean debug = false;
	
	final SkeletonData mData;
	final Array<Bone> mBones;
	final Array<Slot> mSlots;
	final Array<Slot> mDrawOrder;
	Skin mSkin;
	final Color mColor;
	float mTime;
	boolean flipX, flipY;

	public Skeleton (SkeletonData data) {	
		if (data == null) throw new IllegalArgumentException("data cannot be null.");
		this.mData = data;
		mBones = new Array<Bone>(data.mBonesData.size);
		for (BoneData boneData : data.mBonesData) {
			Bone parent = boneData.parent == null ? null : mBones.get(data.mBonesData.indexOf(boneData.parent, true));
			mBones.add(new Bone(boneData, parent));
		}
		mSlots = new Array<Slot>(data.mSlotsData.size);
		mDrawOrder = new Array<Slot>(data.mSlotsData.size);
		for (SlotData slotData : data.mSlotsData) {
			Bone bone = mBones.get(data.mBonesData.indexOf(slotData.boneData, true));
			Slot slot = new Slot(slotData, this, bone);
			mSlots.add(slot);
			mDrawOrder.add(slot);
		}

		mColor = new Color(1, 1, 1, 1);
		
	}

	/** Copy constructor. */
	public Skeleton (Skeleton skeleton) {
		if (skeleton == null) throw new IllegalArgumentException("skeleton cannot be null.");
		mData = skeleton.mData;

		mBones = new Array<Bone>(skeleton.mBones.size);
		for (Bone bone : skeleton.mBones) {
			Bone parent = mBones.get(skeleton.mBones.indexOf(bone.parent, true));
			mBones.add(new Bone(bone, parent));
		}

		mSlots = new Array<Slot>(skeleton.mSlots.size);
		for (Slot slot : skeleton.mSlots) {
			Bone bone = mBones.get(skeleton.mBones.indexOf(slot.bone, true));
			Slot newSlot = new Slot(slot, this, bone);
			mSlots.add(newSlot);
		}

		mDrawOrder = new Array<Slot>(mSlots.size);
		for (Slot slot : skeleton.mDrawOrder)
			mDrawOrder.add(mSlots.get(skeleton.mSlots.indexOf(slot, true)));

		mSkin = skeleton.mSkin;
		mColor = new Color(skeleton.mColor);
		mTime = skeleton.mTime;
	}

	/** Updates the world transform for each bone. */
	public void updateWorldTransform () {
		boolean flipX = this.flipX;
		boolean flipY = this.flipY;
		Array<Bone> bones = this.mBones;
		for (int i = 0, n = bones.size; i < n; i++)
			bones.get(i).updateWorldTransform(flipX, flipY);
	}

	/** Sets the bones and slots to their bind pose values. */
	public void setToBindPose () {
		setBonesToBindPose();
		setSlotsToBindPose();
	}

	public void setBonesToBindPose () {
		Array<Bone> bones = this.mBones;
		for (int i = 0, n = bones.size; i < n; i++)
			bones.get(i).setToBindPose();
	}

	public void setSlotsToBindPose () {
		Array<Slot> slots = this.mSlots;
		for (int i = 0, n = slots.size; i < n; i++)
			slots.get(i).setToBindPose(i);
	}

	public void draw (SpriteBatch batch) {
		Array<Slot> drawOrder = this.mDrawOrder;
		for (int i = 0, n = drawOrder.size; i < n; i++) {
			Slot slot = drawOrder.get(i);
			Attachment attachment = slot.attachment;
			if (attachment != null) {
				attachment.draw(batch, slot);
			}
		}
		
		if (debug){
			drawDebug();
		}
	}
	

	private void drawDebug () {
		for (int i = 0, n = mBones.size; i < n; i++) {
			Bone bone = mBones.get(i);
			if (bone.parent == null) continue;
			float x = bone.data.length * bone.m00 + bone.worldX;
			float y = bone.data.length * bone.m10 + bone.worldY;
			bone.mDebugLine.setPosition(bone.worldX, bone.worldY, x, y);
		}
	
		for (int i = 0, n = mBones.size; i < n; i++) {
			Bone bone = mBones.get(i);
			bone.mDebugRoot.setPosition(bone.worldX,bone.worldY);
		}
		
	} 
	
	public SkeletonData getData () {
		return mData;
	}

	public Array<Bone> getBones () {
		return mBones;
	}

	/** @return May return null. */
	public Bone getRootBone () {
		if (mBones.size == 0) return null;
		return mBones.first();
	}

	/** @return May be null. */
	public Bone findBone (String boneName) {
		if (boneName == null) throw new IllegalArgumentException("boneName cannot be null.");
		Array<Bone> bones = this.mBones;
		for (int i = 0, n = bones.size; i < n; i++) {
			Bone bone = bones.get(i);
			if (bone.data.name.equals(boneName)) return bone;
		}
		return null;
	}

	/** @return -1 if the bone was not found. */
	public int findBoneIndex (String boneName) {
		if (boneName == null) throw new IllegalArgumentException("boneName cannot be null.");
		Array<Bone> bones = this.mBones;
		for (int i = 0, n = bones.size; i < n; i++)
			if (bones.get(i).data.name.equals(boneName)) return i;
		return -1;
	}

	public Array<Slot> getSlots () {
		return mSlots;
	}

	/** @return May be null. */
	public Slot findSlot (String slotName) {
		if (slotName == null) throw new IllegalArgumentException("slotName cannot be null.");
		Array<Slot> slots = this.mSlots;
		for (int i = 0, n = slots.size; i < n; i++) {
			Slot slot = slots.get(i);
			if (slot.data.name.equals(slotName)) return slot;
		}
		return null;
	}

	/** @return -1 if the bone was not found. */
	public int findSlotIndex (String slotName) {
		if (slotName == null) throw new IllegalArgumentException("slotName cannot be null.");
		Array<Slot> slots = this.mSlots;
		for (int i = 0, n = slots.size; i < n; i++)
			if (slots.get(i).data.name.equals(slotName)) return i;
		return -1;
	}

	/** Returns the slots in the order they will be drawn. The returned array may be modified to change the draw order. */
	public Array<Slot> getDrawOrder () {
		return mDrawOrder;
	}

	/** @return May be null. */
	public Skin getSkin () {
		return mSkin;
	}

	/** Sets a skin by name.
	 * @see #setSkin(Skin) */
	public void setSkin (String skinName) {
		Skin skin = mData.findSkin(skinName);
		if (skin == null) throw new IllegalArgumentException("Skin not found: " + skinName);
		setSkin(skin);
	}

	/** Sets the skin used to look up attachments not found in the {@link SkeletonData#getDefaultSkin() default skin}. Attachments
	 * from the new skin are attached if the corresponding attachment from the old skin is currently attached.
	 * @param newSkin May be null. */
	public void setSkin (Skin newSkin) {
		if (mSkin != null && newSkin != null) newSkin.attachAll(this, mSkin);
		mSkin = newSkin;
	}

	/** @return May be null. */
	public Attachment getAttachment (String slotName, String attachmentName) {
		return getAttachment(mData.findSlotIndex(slotName), attachmentName);
	}

	/** @return May be null. */
	public Attachment getAttachment (int slotIndex, String attachmentName) {
		if (attachmentName == null) throw new IllegalArgumentException("attachmentName cannot be null.");
		if (mSkin != null){
			Attachment attachment = mSkin.getAttachment(slotIndex, attachmentName);
			if (attachment != null) return attachment;
		}
	    if (mData.mDefaultSkin != null) return mData.mDefaultSkin.getAttachment(slotIndex, attachmentName);
		return null;
	}

	/** @param attachmentName May be null. */
	public void setAttachment (String slotName, String attachmentName) {
		if (slotName == null) throw new IllegalArgumentException("slotName cannot be null.");
		if (attachmentName == null) throw new IllegalArgumentException("attachmentName cannot be null.");
		for (int i = 0, n = mSlots.size; i < n; i++) {
			Slot slot = mSlots.get(i);
			if (slot.data.name.equals(slotName)) {
				slot.setAttachment(getAttachment(i, attachmentName));
				return;
			}
		}
		throw new IllegalArgumentException("Slot not found: " + slotName);
	}

	public Color getColor () {
		return mColor;
	}

	public boolean getFlipX () {
		return flipX;
	}

	public void setFlipX (boolean flipX) {
		this.flipX = flipX;
	}

	public boolean getFlipY () {
		return flipY;
	}

	public void setFlipY (boolean flipY) {
		this.flipY = flipY;
	}

	public float getTime () {
		return mTime;
	}

	public void setTime (float time) {
		this.mTime = time;
	}

	public void update (float delta) {
		mTime += delta;
	}
}