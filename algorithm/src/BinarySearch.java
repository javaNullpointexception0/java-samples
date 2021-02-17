/**
 * 二分查找
 * 适用条件：
 *  1、必须采用顺序存储结构
 *  2、必须按关键字大小顺序排列
 */
public class BinarySearch {
    public static void main(String[] args) {
        int[] arr = new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        int target = 5;
        int index = leftAndRightClosed(arr, target);
        System.out.println("查找目标所在数据下标：" + index);
    }

    /**
     * 左右为闭区间，循环不变量：[left,right]
     * 所以while(left <= right)、right=miniddle-1或left=middle+1
     */
    private static int leftClosedAndRightOpen(int[]  arr, int target) {
        if (arr == null || arr.length <= 0) {
            return -1;
        }
        int left = 0;
        int right = arr.length;
        while (left < right) {
            int middle = (left + right)/2;
            if (arr[middle] < target) {
                //值比目标小，则往右查找
                left = middle + 1;
            } else if (arr[middle] > target) {
                //值比目标大，则往左查找
                right = middle;
            } else {
                return middle;
            }
        }
        //搜索不到，返回-1
        return -1;
    }

    /**
     * 左右为闭区间，循环不变量：[left,right]
     * 所以while(left <= right)、right=miniddle-1或left=middle+1
     */
    private static int leftAndRightClosed(int[]  arr, int target) {
        if (arr == null || arr.length <= 0) {
            return -1;
        }
        int left = 0;
        int right = arr.length - 1;
        while (left <= right) {
            int middle = (left + right)/2;
            if (arr[middle] < target) {
                //值比目标小，则往右查找
                left = middle + 1;
            } else if (arr[middle] > target) {
                //值比目标大，则往左查找
                right = middle - 1;
            } else {
                return middle;
            }
        }
        //搜索不到，返回-1
        return -1;
    }
}
