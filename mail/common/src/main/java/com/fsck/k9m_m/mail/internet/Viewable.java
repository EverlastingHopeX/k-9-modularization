package com.fsck.k9m_m.mail.internet;

import com.fsck.k9m_m.mail.Message;
import com.fsck.k9m_m.mail.Part;

import java.util.List;

/**
 * Empty marker class interface the class hierarchy used by
 * {@link MessageExtractor#findViewablesAndAttachments(Part, List, List)}
 *
 * @see Viewable.Text
 * @see Viewable.Html
 * @see Viewable.MessageHeader
 * @see Viewable.Alternative
 */
public interface Viewable {
    /**
     * Class representing textual parts of a message that aren't marked as attachments.
     *
     * @see com.fsck.k9m_m.mail.internet.MessageExtractor#isPartTextualBody(com.fsck.k9m_m.mail.Part)
     */
    abstract class Textual implements Viewable {
        private Part mPart;

        public Textual(Part part) {
            mPart = part;
        }

        public Part getPart() {
            return mPart;
        }
    }

    /**
     * Class representing a {@code text/plain} part of a message.
     */
    class Text extends Textual {
        public Text(Part part) {
            super(part);
        }
    }

    class Flowed extends Textual {
        private boolean delSp;

        public Flowed(Part part, boolean delSp) {
            super(part);
            this.delSp = delSp;
        }

        public boolean isDelSp() {
            return delSp;
        }
    }

    /**
     * Class representing a {@code text/html} part of a message.
     */
    class Html extends Textual {
        public Html(Part part) {
            super(part);
        }
    }

    /**
     * Class representing a {@code message/rfc822} part of a message.
     *
     * <p>
     * This is used to extract basic header information when the message contents are displayed
     * inline.
     * </p>
     */
    class MessageHeader implements Viewable {
        private Part mContainerPart;
        private Message mMessage;

        public MessageHeader(Part containerPart, Message message) {
            mContainerPart = containerPart;
            mMessage = message;
        }

        public Part getContainerPart() {
            return mContainerPart;
        }

        public Message getMessage() {
            return mMessage;
        }
    }

    /**
     * Class representing a {@code multipart/alternative} part of a message.
     *
     * <p>
     * Only relevant {@code text/plain} and {@code text/html} children are stored in this container
     * class.
     * </p>
     */
    class Alternative implements Viewable {
        private List<Viewable> mText;
        private List<Viewable> mHtml;

        public Alternative(List<Viewable> text, List<Viewable> html) {
            mText = text;
            mHtml = html;
        }

        public List<Viewable> getText() {
            return mText;
        }

        public List<Viewable> getHtml() {
            return mHtml;
        }
    }
}
