package com.atguigu.gmall.item;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnel;
import com.google.common.hash.Funnels;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

public class BloomTest {


    @Test
    public void testBloom() {
        Funnel<CharSequence> charSequenceFunnel = Funnels.stringFunnel(StandardCharsets.UTF_8);
        BloomFilter<CharSequence> charSequenceBloomFilter
                = BloomFilter.create(charSequenceFunnel, 1000000, 0.0000001);

        charSequenceBloomFilter.put("www.baidu.com");
        charSequenceBloomFilter.put("www.qq.com");
        charSequenceBloomFilter.put("www.jd.com");


        String url = "www.jd.com";
        boolean b = charSequenceBloomFilter.mightContain(url);
        System.out.println(b);
    }
}
