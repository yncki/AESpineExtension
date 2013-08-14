package com.spine;

import android.content.Context;

import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;

public interface AttachmentLoader {
	public Attachment newAttachment (AttachmentType type, String name);
	public Context getContext();
	public BitmapTextureAtlas getAtlas();
}
