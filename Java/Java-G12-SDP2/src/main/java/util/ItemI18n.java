package util;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ItemI18n<T extends Comparable<T>> implements Comparable<ItemI18n<T>> {
    @EqualsAndHashCode.Include
    private T value;
    
    @Getter
    private String label;

    public ItemI18n(T value, String label) {
        this.value = value;
        this.label = label;
    }
    
    public T getValue() {
    	if(value == null) {
    		return null;
    	}
    	return value;
    }

    @Override
    public int compareTo(ItemI18n<T> other) {
        return this.value.compareTo(other.value);
    }
    
    @Override
    public String toString() {
    	return label;
    }
}