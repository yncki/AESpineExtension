package com.spine.attachments;

import android.content.Context;

import com.spine.Attachment;
import com.spine.AttachmentLoader;
import com.spine.AttachmentType;

import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;

public class TextureAtlasAttachmentLoader implements AttachmentLoader {
	private BitmapTextureAtlas atlas;
	private Context context;

	public TextureAtlasAttachmentLoader (BitmapTextureAtlas mBitmapTextureAtlas, Context context) {
		if (mBitmapTextureAtlas == null) throw new IllegalArgumentException("atlas cannot be null.");
		this.atlas = mBitmapTextureAtlas;
		this.context = context;
	}

	public Attachment newAttachment (AttachmentType type, String name) {
		Attachment attachment = null;
		switch (type) {
		case region:
			attachment = new RegionAttachment(name);
			break;
		default:
			throw new IllegalArgumentException("Unknown attachment type: " + type);
		}
		return attachment;
	}
	
	public Context getContext() {
		return context;
	}
	
	public BitmapTextureAtlas getAtlas() {
		return atlas;
	}
}