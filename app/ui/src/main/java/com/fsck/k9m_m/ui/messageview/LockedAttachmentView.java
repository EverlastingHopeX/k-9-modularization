package com.fsck.k9m_m.ui.messageview;


import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewStub;

import com.fsck.k9m_m.ui.R;
import com.fsck.k9m_m.mailstore.AttachmentViewInfo;
import com.fsck.k9m_m.view.ToolableViewAnimator;


public class LockedAttachmentView extends ToolableViewAnimator implements OnClickListener {
    private ViewStub attachmentViewStub;
    private AttachmentViewInfo attachment;
    private AttachmentViewCallback attachmentCallback;


    public LockedAttachmentView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public LockedAttachmentView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LockedAttachmentView(Context context) {
        super(context);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if (isInEditMode()) {
            return;
        }

        View unlockButton = findViewById(R.id.locked_button);
        unlockButton.setOnClickListener(this);

        attachmentViewStub = findViewById(R.id.attachment_stub);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.locked_button) {
            showUnlockedView();
        }
    }

    private void showUnlockedView() {
        if (attachmentViewStub == null) {
            throw new IllegalStateException("Cannot display unlocked attachment!");
        }

        AttachmentView attachmentView = (AttachmentView) attachmentViewStub.inflate();
        attachmentView.setAttachment(attachment);
        attachmentView.setCallback(attachmentCallback);
        attachmentViewStub = null;

        setDisplayedChild(1);
    }

    public void setAttachment(AttachmentViewInfo attachment) {
        this.attachment = attachment;
    }

    public void setCallback(AttachmentViewCallback attachmentCallback) {
        this.attachmentCallback = attachmentCallback;
    }
}
