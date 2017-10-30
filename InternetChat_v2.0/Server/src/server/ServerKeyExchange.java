package server;

import java.util.ArrayList;

/**
 * Server side of key exchange
 * Procesing methods and information storage for key exchange
 *
 * @author Caroline Zeng
 * @version 1.0.0
 */

public class ServerKeyExchange extends Thread {
    /**
     * Signal to server to begin key exchange
     */
    public static final String KEY = "KEYEXCHANGE";
    /**
     * Signal from server that current values are the private key
     */
    public static final String DONE = "KEYDONE";
    /**
     * Signal from server that key exchange is not done
     */
    public static final String CONTINUE = "CONTINUE";

    /**
     * Base value in key exchange
     */
    int base;
    /**
     * Modulo value in key exchange
     */
    int mod;
    /**
     * Public value pair
     */
    int[] inf;

    /**
     * Constructor for ServerKeyExchange
     */
    public ServerKeyExchange() {}

    /**
     * Generates public values for key exchange, and stores in instance variables
     */
    public void generatePublicVal()
    {
        ArrayList<Integer> primes = primesInRange(
                500 + (int)(Math.random() * 500),
                1000 + (int)(Math.random() * 500));
        mod                       = primes.get((int)(Math.random() * primes.size()));
        ArrayList<Integer> bases  = primitiveRootModulo(mod);
        base                      = bases.get((int)(Math.random() * (bases.size())));
        inf                       = new int[] {base, mod};
    }

    /**
     * Finds all primitive root modulos of a number (less than the number)
     * @param mod   number
     * @return      ArrayList of all primitive root modulos
     */
    public ArrayList<Integer> primitiveRootModulo(int mod)
    {
        int res;
        int ord                  = 0;
        ArrayList<Integer> roots = new ArrayList<>();

        for (int root = 2; root < mod; root++)
        {
            res = (int) Math.pow(root, ord);
            res %= mod;

            while (res > 1)
            {
                ord++;
                res *= root;
                res %= mod;
            }

            if (ord == totient(mod)) roots.add(root);
            ord = 1;
        }

        return roots;
    }

    /**
     * Checks if two numbers are coprime, or relatively prime
     * @param a number
     * @param b number
     * @return  true if they share no factors;
     *          false if they share a factor
     */
    public boolean coprime(int a, int b)
    {
        ArrayList<Integer> afac   = factors(a);
        ArrayList<Integer> bfac   = factors(b);
        ArrayList<Integer> common = (ArrayList<Integer>) afac.clone();
        common.retainAll(bfac);
        if (common.size() > 1) return false;
        return true;
    }

    /**
     * Euler's totient function.
     * Finds the number of integers below an integer that are relatively prime to the number.
     * @param a integer
     * @return  number of integers relatively prime to and less than the number
     */
    public int totient(int a)
    {
        int num = 0;

        for (int i = 1; i < a; i++)
        {
            if (coprime(a, i)) num++;
        }

        return num;
    }

    /**
     * Finds the factors of a number
     * @param a number
     * @return  all factors of a number
     */
    public ArrayList<Integer> factors(int a)
    {
        ArrayList<Integer> factors = new ArrayList<>();
        factors.add(1);

        for (int i = 2; i <= a; i++)
        {
            if (a % i == 0) factors.add(i);
        }

        return factors;
    }

    /**
     * Finds the multiplicative order of base (mod (mod)).
     * Finds the first exponent of base that is defined as 1 (mod (mod)).
     * @param base  base
     * @param mod   modulo
     * @return      multiplicative order of the base (mod (mod))
     */
    public int multiplicativeOrder(int base, int mod)
    {
        int ord = 1;
        int res = (int) Math.pow(base, ord);
        res    %= mod;

        while (res > 1)
        {
            ord++;
            res *= base;
            res %= mod;
        }

        return ord;
    }

    /**
     * Finds all primes between a (inclusive) and b (exclusive)
     * @param a lower bound
     * @param b upper bound
     * @return  ArrayList of all primes between a and b, excluding b
     */
    public ArrayList<Integer> primesInRange(int a, int b)
    {
        ArrayList<Integer> primes = new ArrayList<>();

        outerloop:
        for (int i = a; i < b; i++)
        {
            for (int j = 2; j <= i / 2; j++)
            {
                if (i % j == 0) continue outerloop;
            }

            primes.add(i);
        }

        return primes;
    }

    /**
     * Checks if a number is a primitive root modulo of another number
     * @param p     base
     * @param mod   modulo
     * @return      true if base is a primitive root modulo of mod;
     *              false if base is not
     */
    public boolean isPrimitiveRootModulo(int p, int mod)
    {
        if (multiplicativeOrder(p, mod) == totient(mod)) return true;
        return false;
    }

    /**
     * Generates public values
     */
    public void run()
    {
        generatePublicVal();
        inf = new int[] {base, mod};
    }
}