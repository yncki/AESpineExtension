package com.spine;

import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;

import android.content.Context;

public interface AttachmentLoader {
	public Attachment newAttachment (AttachmentType type, String name);
	public Context getContext();
	public BitmapTextureAtlas getAtlas();
}
