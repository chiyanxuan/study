package com.nil.pagehelp1.bean;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


public class PageUtil {
    /**
     * 默认的分页大小
     *
     */
    public static final int PAGE_SIZE = 10;

    /**
     * 默认起始页大小
     *
     */
    public static final int CURRENT_PAGE = 1;

    /**
     * 获取返回分页数据量
     * @param size
     * @return
     */
    public static int getLimit(int size) {
        if (size < 1) {
            return PAGE_SIZE;
        }
        return size;
    }

    /**
     * 获取返回分页偏移量
     * @param page
     * @param size
     * @return
     */
    public static int getOffset(int page, int size) {
        if (page < 1) {
            page = 1;
        }
        return (page - 1) * size;
    }

    public static <T> List<T> listToPage(List<T> list, int page, int size) {
        int offset = getOffset(page, size);
        int limit = getLimit(size);

        if (list.size() < offset) {
            return Collections.emptyList();
        }

        if (list.size() > offset && list.size() < limit) {
            limit = list.size();
        }

        return list.stream().skip(offset).limit(limit).collect(Collectors.toList());
    }

    public static int getPageTotal(int total, int size) {
        if (total%size == 0) {
            return total/size;
        } else {
            return total/size + 1;
        }
    }
}
