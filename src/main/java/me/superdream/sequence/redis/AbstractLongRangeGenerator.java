/*
 * Copyright (c) 2017 SuperDream Inc. <http://www.superdream.me>
 */

package me.superdream.sequence.redis;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import static java.util.Arrays.*;

/**
 * 基于指定范围随机获取的长整形对序列生成器
 *
 * @author <a href="mailto:517926804@qq.com">Mike Chen</a>
 * @version 0.1.0
 * @since 0.1.0
 */
public abstract class AbstractLongRangeGenerator extends AbstractGenerator<Long> {
    protected final int step; // 步长
    protected int maxStepValue; // 最大步长值

    private final long values[];
    private final long excludes[];

    public AbstractLongRangeGenerator(String hname, String hkey, int step) {
        super(hname, hkey);
        this.step = step;

        this.values = new long[step];
        this.excludes = new long[step];
        fill(excludes, -1L);

        createRange(step);
    }

    @Override
    public Long next() {
        synchronized (this) {
            long id = -1;
            for (int i = 0; i < values.length; i++) {
                long excludeIndex = excludes[i];
                if (excludeIndex == -1) {
                    excludes[i] = i;
                    id = values[i];
                    break;
                }
            }

            checkFinish();

            return id;
        }
    }

    private void createRange(int step) {
        Random random = ThreadLocalRandom.current();

        refreshMaxStepValue();

        int min = maxStepValue - step;

        for (int i = 0; i < step; i++) {
            values[i] = min + i;
        }

        for (int i = 0; i < step; i++) {
            int temp1 = random.nextInt(step); //随机产生一个位置
            int temp2 = random.nextInt(step); //随机产生另一个位置

            if (temp1 != temp2) {
                long temp3 = values[temp1];
                values[temp1] = values[temp2];
                values[temp2] = temp3;
            }
        }
    }

    private void checkFinish() {
        long[] cs = excludes.clone();
        sort(cs);
        int index = binarySearch(cs, -1);
        if (index < 0) {
            fill(excludes, -1L);
            createRange(step);
        }
    }

    protected abstract void refreshMaxStepValue();
}
