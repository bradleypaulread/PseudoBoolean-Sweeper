import java.math.*;
import org.apache.commons.math3.*;
import org.apache.commons.math3.fraction.BigFraction;

public class BigRational {

    public static void main(String[] args) {
        BigInteger num1 = new BigInteger("1100909");
        BigInteger den1 = new BigInteger("98980989");
        
        BigInteger num2 = new BigInteger("5");
        BigInteger den2 = new BigInteger("9");
        
        BigFraction bf1 = new BigFraction(num1, den1);
        BigFraction bf2 = new BigFraction(num2, den2);
        
        System.out.println(bf1);
        System.out.println(bf2);
        System.out.println();
        System.out.print("" + bf1 + " divided by " + bf2 + " = ");
        System.out.print(bf1.divide(bf2) + " (as Double: " + bf1.divide(bf2).doubleValue() + ")");
        
    }
}