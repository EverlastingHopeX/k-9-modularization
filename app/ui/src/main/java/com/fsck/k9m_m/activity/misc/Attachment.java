package com.fsck.k9m_m.activity.misc;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


/**
 * Container class for information about an attachment.
 *
 * This is used by {@link com.fsck.k9m_m.activity.MessageCompose} to fetch and manage attachments.
 */
public class Attachment implements Parcelable, com.fsck.k9m_m.message.Attachment {
    /**
     * The URI pointing to the source of the attachment.
     *
     * In most cases this will be a {@code content://}-URI.
     */
    public final Uri uri;

    /**
     * The current loading state.
     */
    public final LoadingState state;

    /**
     * The ID of the loader that is used to load the metadata or contents.
     */
    public final int loaderId;

    /**
     * The content type of the attachment.
     *
     * Valid iff {@link #state} is {@link LoadingState#METADATA} or {@link LoadingState#COMPLETE}.
     */
    public final String contentType;

    /**
     * {@code true} if we allow MIME types of {@code message/*}, e.g. {@code message/rfc822}.
     */
    public final boolean allowMessageType;

    /**
     * The (file)name of the attachment.
     *
     * Valid iff {@link #state} is {@link LoadingState#METADATA} or {@link LoadingState#COMPLETE}.
     */
    public final String name;

    /**
     * The size of the attachment.
     *
     * Valid iff {@link #state} is {@link LoadingState#METADATA} or {@link LoadingState#COMPLETE}.
     */
    public final Long size;

    /**
     * The name of the temporary file containing the local copy of the attachment.
     *
     * Valid iff {@link #state} is {@link LoadingState#COMPLETE}.
     */
    public final String filename;

    @NotNull
    @Override
    public LoadingState getState() {
        return state;
    }

    @Nullable
    @Override
    public String getFileName() {
        return filename;
    }

    @Nullable
    @Override
    public String getContentType() {
        return contentType;
    }

    @Nullable
    @Override
    public String getName() {
        return name;
    }

    @Nullable
    @Override
    public Long getSize() {
        return size;
    }

    private Attachment(Uri uri, LoadingState state, int loaderId, String contentType, boolean allowMessageType,
            String name, Long size, String filename) {
        this.uri = uri;
        this.state = state;
        this.loaderId = loaderId;
        this.contentType = contentType;
        this.allowMessageType = allowMessageType;
        this.name = name;
        this.size = size;
        this.filename = filename;
    }

    private Attachment(Parcel in) {
        uri = in.readParcelable(Uri.class.getClassLoader());
        state = (LoadingState) in.readSerializable();
        loaderId = in.readInt();
        contentType = in.readString();
        allowMessageType = in.readInt() != 0;
        name = in.readString();
        if (in.readInt() != 0) {
            size = in.readLong();
        } else {
            size = null;
        }
        filename = in.readString();
    }

    public static Attachment createAttachment(Uri uri, int loaderId, String contentType, boolean allowMessageType) {
        return new Attachment(uri, Attachment.LoadingState.URI_ONLY, loaderId, contentType, allowMessageType, null,
                null, null);
    }

    public Attachment deriveWithMetadataLoaded(String usableContentType, String name, long size) {
        if (state != Attachment.LoadingState.URI_ONLY) {
            throw new IllegalStateException("deriveWithMetadataLoaded can only be called on a URI_ONLY attachment!");
        }
        return new Attachment(uri, Attachment.LoadingState.METADATA, loaderId, usableContentType, allowMessageType,
                name, size, null);
    }

    public Attachment deriveWithLoadCancelled() {
        if (state != Attachment.LoadingState.METADATA) {
            throw new IllegalStateException("deriveWitLoadCancelled can only be called on a METADATA attachment!");
        }
        return new Attachment(uri, Attachment.LoadingState.CANCELLED, loaderId, contentType, allowMessageType, name,
                size, null);
    }

    public Attachment deriveWithLoadComplete(String absolutePath) {
        if (state != Attachment.LoadingState.METADATA) {
            throw new IllegalStateException("deriveWithLoadComplete can only be called on a METADATA attachment!");
        }
        return new Attachment(uri, Attachment.LoadingState.COMPLETE, loaderId, contentType, allowMessageType, name,
                size, absolutePath);
    }

    // === Parcelable ===

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(uri, flags);
        dest.writeSerializable(state);
        dest.writeInt(loaderId);
        dest.writeString(contentType);
        dest.writeInt(allowMessageType ? 1 : 0);
        dest.writeString(name);
        if (size != null) {
            dest.writeInt(1);
            dest.writeLong(size);
        } else {
            dest.writeInt(0);
        }
        dest.writeString(filename);
    }

    public static final Parcelable.Creator<Attachment> CREATOR =
            new Parcelable.Creator<Attachment>() {
        @Override
        public Attachment createFromParcel(Parcel in) {
            return new Attachment(in);
        }

        @Override
        public Attachment[] newArray(int size) {
            return new Attachment[size];
        }
    };
}
