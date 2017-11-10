public class Loop {
    
    public static void main (String[] args) 
        int[] a = {1, 5, 9, 321, 5, 9};
        
        int sum = 0;
        for (int i = 0; i <= a.length; i++) {
            sum = sum + a[i];
        }
        
        System.out.println("sum = " + sum);
    }
}