package common;

import java.io.Serializable;

/**
 * SearchRange - Represents a range of password indices to search
 * Used to divide work between servers and threads
 */
public class SearchRange implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private final long startIndex;  // Inclusive start index
    private final long endIndex;    // Exclusive end index
    
    public SearchRange(long startIndex, long endIndex) {
        this.startIndex = startIndex;
        this.endIndex = endIndex;
    }
    
    public long getStartIndex() {
        return startIndex;
    }
    
    public long getEndIndex() {
        return endIndex;
    }
    
    public long getSize() {
        return endIndex - startIndex;
    }
    
    @Override
    public String toString() {
        return String.format("Range[%d - %d] (size: %d)", startIndex, endIndex, getSize());
    }
}
