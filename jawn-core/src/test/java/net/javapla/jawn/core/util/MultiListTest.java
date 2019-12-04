package net.javapla.jawn.core.util;

import static com.google.common.truth.Truth.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.junit.Test;

public class MultiListTest {


    @Test
    public void mapToList() {
        MultiList<String> list = new MultiList<>();
        
        list.put("a", Arrays.asList("1","11","111"));
        list.put("b", Arrays.asList("2","22","222"));
        
        MultiList<Integer> list2 = list.map(Integer::valueOf);
        
        assertThat(list2.size()).isEqualTo(list.size());
        assertThat(list2.list("a")).containsExactly(1,11,111);
        assertThat(list2.list("b")).containsExactly(2,22,222);
    }
    
    @Test
    public void mapConstructor() {
        Map<String, List<String>> map = CollectionUtil.map("key1", Arrays.asList("1","11","111"), "key2", Arrays.asList("2","22","222"), "key3", Arrays.asList("3"));
        
        MultiList<String> list = new MultiList<String>(map);
        
        assertThat(list.size()).isEqualTo(3);
        
        assertThat(list.first("key1")).isEqualTo("1");
        assertThat(list.last("key1")).isEqualTo("111");
        
        assertThat(list.first("key3")).isEqualTo("3");
        assertThat(list.last("key3")).isEqualTo("3");
    }
    
    @Test
    public void entryConstructor() {
        Set<Entry<String,List<String>>> set = CollectionUtil.map("key1", Arrays.asList("1","11","111"), "key2", Arrays.asList("2","22","222"), "key3", Arrays.asList("3")).entrySet();
        
        MultiList<List<String>> list = new MultiList<>(new ArrayList<>(set));
        
        assertThat(list.size()).isEqualTo(3);
    }
    
    @Test
    public void putMultipleValues() {
        MultiList<String> list = MultiList.<String>empty();
        
        list.put("key", "test1");
        list.put("key", "test2", "test3");
        
        assertThat(list.size()).isEqualTo(1);
        assertThat(list.contains("key")).isTrue();
        assertThat(list.list("key")).hasSize(3);
        
        assertThat(list.contains("anything")).isFalse();
    }
    
    @Test
    public void isEmpty() {
        MultiList<Object> empty = MultiList.empty();
        assertThat(empty.isEmpty()).isTrue();
        
        empty.put("key", new Object());
        assertThat(empty.isEmpty()).isFalse();
    }

}
