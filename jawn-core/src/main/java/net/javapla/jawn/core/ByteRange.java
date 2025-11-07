package net.javapla.jawn.core;

public interface ByteRange {
    
    String HEADER_PREFIX = "bytes=";

    /**
     * Parse the "Range" header. Examples:
     * <p>- bytes=64-
     * <p>- bytes=0-100
     * <p>- bytes=0-100, 120-128, 148-160, ... (not currently supported by the framework)
     */
    static ByteRange parse(String value, long contentLength) {
        if (contentLength <= 0 || value == null) {
            // NO-OP
            return new NoRange(contentLength);
        }
        
        if (!value.startsWith(HEADER_PREFIX)) {
            return new NotSatisfiableRange(value, contentLength);
        }
        
        try {
            long[] range = {-1, -1};
            int r = 0;
            int len = value.length();
            int i = HEADER_PREFIX.length();
            int offset = i;
            char ch;
            // Only Single Byte Range Requests:
            while (i < len && (ch = value.charAt(i)) != ',') {
                if (ch == '-') {
                    if (offset < i) {
                        range[r] = Long.parseLong(value.substring(offset, i).trim());
                    }
                    offset = i + 1;
                    r += 1;
                }
                i += 1;
            }
            if (offset < i) {
                if (r == 0) {
                    return new NotSatisfiableRange(value, contentLength);
                }
                range[r++] = Long.parseLong(value.substring(offset, i).trim());
            }
            if (r == 0 || (range[0] == -1 && range[1] == -1)) {
                return new NotSatisfiableRange(value, contentLength);
            }

            long start = range[0];
            long end = range[1];
            if (start == -1) {
                start = contentLength - end;
                end = contentLength - 1;
            }
            if (end == -1 || end > contentLength - 1) { // Should it return a NotSatisfiableRange instead when end > contentLength?
                end = contentLength - 1;
            }
            if (start > end) {
                return new NotSatisfiableRange(value, contentLength);
            }
            // offset
            long limit = (end - start + 1);
            return new SingleRange(value, start, end, limit, "bytes " + start + "-" + end + "/" + contentLength);
        } catch (NumberFormatException expected) {
            return new NotSatisfiableRange(value, contentLength);
        }
    }
    
    long start();
    long end();
    long contentLength();
    
    /**
     * @return Value for the "Content-Range" response header
     */
    String contentRange();
    Status status();
    ByteRange apply(Context ctx);
    
    class SingleRange implements ByteRange {
        
        private final String value;
        private final long start;
        private final long end;
        private final long contentLength;
        private final String contentRange;

        private SingleRange(String value, long start, long end, long contentLength, String contentRange) {
            this.value = value;
            this.start = start;
            this.end = end;
            this.contentLength = contentLength;
            this.contentRange = contentRange;
        }

        @Override
        public long start() {
            return start;
        }

        @Override
        public long end() {
            return end;
        }

        @Override
        public long contentLength() {
            return contentLength;
        }

        @Override
        public String contentRange() {
            return contentRange;
        }

        @Override
        public Status status() {
            return Status.PARTIAL_CONTENT;
        }

        @Override
        public ByteRange apply(Context ctx) {
            ctx.resp().header("Accept-Ranges", "bytes");
            ctx.resp().header("Content-Range", contentRange());
            ctx.resp().contentLength(contentLength);
            ctx.resp().status(Status.PARTIAL_CONTENT);
            return this;
        }
        
        @Override
        public String toString() {
            return value;
        }
        
    }
    
    class NoRange implements ByteRange {
        private final long contentLength;

        private NoRange(long contentLength) {
            this.contentLength = contentLength;
        }

        @Override
        public long start() {
            return 0;
        }

        @Override
        public long end() {
            return contentLength;
        }

        @Override
        public long contentLength() {
            return contentLength;
        }
        
        @Override
        public String contentRange() {
            return "bytes */" + contentLength;
        }

        @Override
        public Status status() {
            return Status.OK;
        }

        @Override
        public ByteRange apply(Context ctx) {
            return this;
        }
        
        @Override
        public String toString() {
            return "no range";
        }
    }
    
    class NotSatisfiableRange implements ByteRange {
        
        private final String value;
        private final long contentLength;

        private NotSatisfiableRange(String value, long contentLength) {
            this.value = value;
            this.contentLength = contentLength;
        }

        @Override
        public long start() {
            return -1;
        }

        @Override
        public long end() {
            return -1;
        }

        @Override
        public long contentLength() {
            return contentLength;
        }

        @Override
        public String contentRange() {
            return "bytes */" + contentLength;
        }

        @Override
        public Status status() {
            return Status.REQUESTED_RANGE_NOT_SATISFIABLE;
        }

        @Override
        public ByteRange apply(Context ctx) {
            throw Up.because(Status.REQUESTED_RANGE_NOT_SATISFIABLE, value);
        }
        
    }
}
