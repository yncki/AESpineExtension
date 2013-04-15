package com.spine;

import com.badlogic.gdx.utils.Array;

public class SkeletonData {
	
	final Array<Animation> mAnimations = new Array<Animation>();
	final Array<BoneData> mBonesData = new Array<BoneData>(); // Ordered parents first.
	final Array<SlotData> mSlotsData = new Array<SlotData>(); // Bind pose draw order.
	final Array<Skin> mSkins = new Array<Skin>();
	Skin mDefaultSkin;

	public void clear () {
		mBonesData.clear();
		mSlotsData.clear();
		mSkins.clear();
		mDefaultSkin = null;
	}

	// --- Bones.

	public void addBone (BoneData bone) {
		if (bone == null) throw new IllegalArgumentException("bone cannot be null.");
		mBonesData.add(bone);
	}

	public Array<BoneData> getBones () {
		return mBonesData;
	}

	/** @return May be null. */
	public BoneData findBone (String boneName) {
		if (boneName == null) throw new IllegalArgumentException("boneName cannot be null.");
		Array<BoneData> bones = this.mBonesData;
		for (int i = 0, n = bones.size; i < n; i++) {
			BoneData bone = bones.get(i);
			if (bone.name.equals(boneName)) return bone;
		}
		return null;
	}

	/** @return -1 if the bone was not found. */
	public int findBoneIndex (String boneName) {
		if (boneName == null) throw new IllegalArgumentException("boneName cannot be null.");
		Array<BoneData> bones = this.mBonesData;
		for (int i = 0, n = bones.size; i < n; i++)
			if (bones.get(i).name.equals(boneName)) return i;
		return -1;
	}

	// --- Slots.

	public void addSlot (SlotData slot) {
		if (slot == null) throw new IllegalArgumentException("slot cannot be null.");
		mSlotsData.add(slot);
	}

	public Array<SlotData> getSlots () {
		return mSlotsData;
	}

	/** @return May be null. */
	public SlotData findSlot (String slotName) {
		if (slotName == null) throw new IllegalArgumentException("slotName cannot be null.");
		Array<SlotData> slots = this.mSlotsData;
		for (int i = 0, n = slots.size; i < n; i++) {
			SlotData slot = slots.get(i);
			if (slot.name.equals(slotName)) return slot;
		}
		return null;
	}

	/** @return -1 if the bone was not found. */
	public int findSlotIndex (String slotName) {
		if (slotName == null) throw new IllegalArgumentException("slotName cannot be null.");
		Array<SlotData> slots = this.mSlotsData;
		for (int i = 0, n = slots.size; i < n; i++)
			if (slots.get(i).name.equals(slotName)) return i;
		return -1;
	}

	// --- Skins.

	/** @return May be null. */
	public Skin getDefaultSkin () {
		return mDefaultSkin;
	}

	/** @param defaultSkin May be null. */
	public void setDefaultSkin (Skin defaultSkin) {
		this.mDefaultSkin = defaultSkin;
	}

	public void addSkin (Skin skin) {
		if (skin == null) throw new IllegalArgumentException("skin cannot be null.");
		mSkins.add(skin);
	}

	/** @return May be null. */
	public Skin findSkin (String skinName) {
		if (skinName == null) throw new IllegalArgumentException("skinName cannot be null.");
		for (Skin skin : mSkins)
			if (skin.name.equals(skinName)) return skin;
		return null;
	}

	/** Returns all skins, including the default skin. */
	public Array<Skin> getSkins () {
		return mSkins;
	}
	
	
	public void addAnimation (Animation animation) {
	    if (animation == null) throw new IllegalArgumentException("animation cannot be null.");
		    mAnimations.add(animation);
	}
		
		  public Array<Animation> getAnimations () {
		    return mAnimations;
		  }
		
		  /** @return May be null. */
		  public Animation findAnimation (String animationName) {
		    if (animationName == null) throw new IllegalArgumentException("animationName cannot be null.");
		    Array<Animation> animations = this.mAnimations;
		    for (int i = 0, n = animations.size; i < n; i++) {
		      Animation animation = animations.get(i);
		      if (animation.getName().equals(animationName)) return animation;
		    }
		    return null;
		  }
		
}
