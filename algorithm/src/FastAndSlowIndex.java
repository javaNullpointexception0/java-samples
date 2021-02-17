/**
 * 快慢指针发（双指针法）
 */
public class FastAndSlowIndex {
    public static void main(String[] args) {
        int[] arr = new int[]{1, 2, 3, 2, 5, 6, 3, 8, 3, 10};
        int target = 3;
        fastAndSlowIndex(arr, target);
    }

    private static int fastAndSlowIndex( int[] arr, int target) {
        int slowIndex = 0;
        for (int fastIndex = 0; fastIndex < arr.length; fastIndex++) {
            if (arr[fastIndex] != target) {
                arr[slowIndex++] = arr[fastIndex];
            }
        }
        System.out.println("最后数组长度：" + slowIndex);
    }
}
