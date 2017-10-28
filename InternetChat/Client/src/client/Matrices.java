package client;

import java.util.Arrays;

/**
 * Operations on matrices
 *
 * @author Caroline Zeng
 * @version 1.0.0
 */

public class Matrices {
    /*
     * width  = x = Matrices.length    = row
     * height = y = Matrices[n].length = column
     *
     * plaintext being ciphered = float[0][textlength]
     *
     * multiplication result = float[key.x][text.y]
     *
     * get from matrix = Matrices[x][y]
     */

    /**
     * Multiply 2 matrices, given their dimensions allow them to be multiplied together
     * @param mat1  left hand AxB matrix
     * @param mat2  right hand BxC matrix
     * @return      AxC matrix
     */
    public static float[][] matrixMultiply(float[][] mat1, float[][] mat2)
    {
        int retx      = getX(mat2);
        int rety      = getY(mat1);
        float[][] ret = new float[retx][rety];

        for (int x = 0; x < retx; x++)
        {

            for (int y = 0; y < rety; y++)
            {

                int sum     = 0;
                float[] row = getRow(mat1, y);
                float[] col = getCol(mat2, x);

                for (int i = 0; i < row.length; i++)
                {
                    sum += row[i] * col[i];
                }

                ret[x][y] = sum;

            }

        }

        return ret;
    }

    /**
     * Multiplies a matrix by a scalar
     * @param mat   matrix to be multiplied
     * @param s     scalar to multiply matrix by
     * @return      matrix multiplied by scalar
     */
    public static float[][] scalarMultiply(float[][] mat, float s)
    {
        int x         = getX(mat);
        int y         = getY(mat);
        float[][] ret = new float[x][y];

        for (int i = 0; i < x; i++)
        {

            for (int j = 0; j < y; j++)
            {
                ret[i][j] = mat[i][j] * s;
            }

        }

        return ret;
    }

    /**
     * Divide a matrix by a scalar
     * @param mat   matrix to be divided
     * @param s     scalar to divide matrix by
     * @return      matrix divided by scalar
     */
    public static float[][] scalarDivide(float[][] mat, float s)
    {
        int x         = getX(mat);
        int y         = getY(mat);
        float[][] ret = new float[x][y];
        for (int i = 0; i < x; i++)
        {

            for (int j = 0; j < y; j++)
            {
                ret[i][j] = mat[i][j] / s;
            }

        }

        return ret;
    }

    /**
     * Modulos each value in the matrix by the given value
     * @param mat   matrix to be operated on
     * @param mod   modulo value
     * @return      matrix moduloed by the given value
     */
    public static float[][] modMat(float[][] mat, float mod)
    {
        int x         = getX(mat);
        int y         = getY(mat);
        float[][] ret = new float[x][y];

        for (int i = 0; i < x; i++)
        {
            for (int j = 0; j < y; j++)
            {
                ret[i][j] = mat[i][j] % mod;
            }
        }
        return ret;
    }

    /**
     * After a modulo, makes all values the positive modulo.
     * When using a modulo on a float value, a negative number can be returned
     * @param mat   matrix to be operated on
     * @param mod   mod value
     * @return      matrix with all positive mod values
     */
    public static float[][] extModMat(float[][] mat, float mod)
    {
        mat      = modMat(mat, mod);
        int matx = getX(mat);
        int maty = getY(mat);

        for (int i = 0; i < matx; i++)
        {
            for (int j = 0; j < maty; j++)
            {
                if (mat[i][j] < 0) mat[i][j] += mod;
            }
        }
        return mat;
    }

    /**
     * Gets the matrix of minors for a matrix
     * @param mat   matrix to find matrix of minors for
     * @return      matrix of minors
     */
    public static float[][] getMatMinors(float[][] mat)
    {
        int dim       = mat.length;
        float[][] ret = new float[dim][dim];

        for (int x = 0; x < dim; x++)
        {
            for (int y = 0; y < dim; y++)
            {
                boolean passx   = false;
                float[][] small = new float[dim - 1][dim - 1];

                for (int i = 0; i < dim; i++)
                {
                    boolean passy = false;

                    if (i == x)
                    {
                        passx = true;
                        continue;
                    }

                    int indx = i;
                    if (passx) indx--;

                    for (int j = 0; j < dim; j++)
                    {
                        if (j == y)
                        {
                            passy = true;
                            continue;
                        }

                        int indy = j;
                        if (passy) indy--;
                        small[indx][indy] = mat[i][j];
                    }
                }
                ret[x][y] = getDet(small);
            }
        }
        return ret;
    }

    /**
     * Gets the matrix of cofactors for a matrix
     * @param mat   matrix to find matrix of cofactors for
     * @return      matrix of cofactors
     */
    public static float[][] getMatCofactors(float[][] mat)
    {
        int dim          = mat.length;
        float[][] ret    = new float[dim][dim];
        float[][] matmin = getMatMinors(mat);

        for (int x = 0; x < dim; x++)
        {
            for (int y = 0; y < dim; y++)
            {
                float val = matmin[x][y];
                if ((x + y) % 2 == 1) val = -val;
                ret[x][y] = val;
            }
        }
        ret = transpose(ret);
        return ret;
    }

    /**
     * Gets the inverse of a matrix
     * @param mat   matrix to find inverse for
     * @return      inverse of matrix
     */
    public static float[][] getInverse(float[][] mat)
    {
        float det   = getDet(mat);
        float[][] C = getMatCofactors(mat);
        return scalarDivide(C, det);
    }

    /**
     * Gets the transpose of a matrix.
     * Essentially flips matrix along its diagonal
     * @param mat   matrix to find transpose for
     * @return      transpose of matrix
     */
    public static float[][] transpose(float[][] mat)
    {
        int dim       = mat.length;
        float[][] ret = new float[dim][dim];

        for (int i = 0; i < dim; i++)
        {
            for (int j = 0; j < dim; j++)
            {
                ret[i][j] = mat[j][i];
            }
        }
        return ret;
    }

    /**
     * Gets determinant of matrix
     * @param mat   matrix to find determinant of
     * @return      determinant of matrix
     */
    public static float getDet(float[][] mat)
    {
        float ret = 0;
        int dim   = mat.length;

        if (dim > 2)
        {
            float[] toprow = getRow(mat, 0);

            for (int i = 0; i < dim; i++)
            {
                float t         = toprow[i];
                float[][] small = new float[dim - 1][dim - 1];
                boolean passed  = false;

                for (int j = 0; j < dim; j++)
                {
                    if (j == i)
                    {
                        passed = true;
                        continue;
                    }

                    int index = j;
                    if (passed) index--;
                    small = setCol(small, Arrays.copyOfRange(getCol(mat, j), 1, dim), index);
                }
                if (i % 2 == 0) ret += t * getDet(small);
                else ret -= t * getDet(small);
            }
        } else
        {
            ret = mat[0][0] * mat[1][1] - mat[1][0] * mat[0][1];
        }
        return ret;
    }

    /**
     * Gets the modular inverse of a matrix
     * @param mat   matrix to find the modular inverse for
     * @return      modular inverse of the matrix
     */
    public static float[][] matModInv(float[][] mat)
    {
        float det     = getDet(mat);
        float cons    = modInv((int)det, 97);
        float[][] C   = getMatCofactors(mat);
        float[][] inv = scalarMultiply(C, cons);
        inv           = extModMat(inv, 97);
        return inv;
    }

    /**
     * Transforms a String to a matrix
     * @param text  String to transform
     * @return      Array of matrices corresponding to the text
     */
    public static float[][][] transformTextToMat(String text)
    {
        int a = text.length() / 3;
        if (text.length() % 3 != 0) a++;
        float[][][] ret = new float[a][1][3];

        for (int i = 0; i < text.length(); i++)
        {
            int fragment = i / 3;
            ret[fragment][0][i % 3] = (int)(text.charAt(i)) - 32;
        }
        return ret;
    }

    /**
     * Transforms a matrix to text
     * @param mat   matrix to transform to text
     * @return      text corresponding to the matrix
     */
    public static String transformMatToText(float[][] mat)
    {
        String s = "";

        for (int i = 0; i < mat[0].length; i++)
        {
            s += (char)((int)mat[0][i] % 97 + 32);
        }
        return s;
    }

    /**
     * Generates a random key matrix
     * @param dimension size of key matrix
     * @return          a dimension x dimension matrix of random values
     */
    public static float[][] generateKey(int dimension)
    {
        float det = 0;
        float[][] key = new float[dimension][dimension];

        while (det == 0)
        {
            for (int x = 0; x < dimension; x++)
            {
                for (int y = 0; y < dimension; y++)
                {
                    key[x][y] = (int)(Math.random() * 97);
                }
            }
            det = getDet(key);
        }
        return key;
    }

    /**
     * Gets a row of a matrix
     * @param mat   matrix to get row from
     * @param y     y-value of the row
     * @return      row of matrix
     */
    public static float[] getRow(float[][] mat, int y)
    {
        float[] ret = new float[mat.length];

        for (int i = 0; i < mat.length; i++)
        {
            float[] col = mat[i];
            ret[i]      = col[y];
        }
        return ret;
    }

    /**
     * Gets a column of a matrix
     * @param mat   matrix to get column from
     * @param x     x-value of column
     * @return      column of matrix
     */
    public static float[] getCol(float[][] mat, int x)
    {
        float[] ret = mat[x];
        return ret;
    }

    /**
     * Sets a row of a matrix to given values
     * @param mat   matrix to set row
     * @param row   row to set matrix's row to
     * @param y     y-value of row in matrix
     * @return      matrix with new values in given row
     */
    public static float[][] setRow(float[][] mat, float[] row, int y)
    {
        for (int i = 0; i < mat.length; i++)
        {
            mat[i][y] = row[i];
        }
        return mat;
    }

    /**
     * Sets a column of a matrix to given values
     * @param mat   matrix to set column
     * @param col   column to set matrix's column to
     * @param x     x-value of column in matrix
     * @return      matrix with new values in given column
     */
    public static float[][] setCol(float[][] mat, float[] col, int x)
    {
        mat[x] = col;
        return mat;
    }

    /**
     * Gets x-dimension of a matrix
     * @param mat   matrix to find x-dimension of
     * @return      x-dimension of matrix
     */
    public static int getX(float[][] mat)
    {
        return mat.length;
    }

    /**
     * Gets y-dimension of matrix
     * @param mat   matrix to get y-dimension of
     * @return      y-dimension of matrix
     */
    public static int getY(float[][] mat)
    {
        return mat[0].length;
    }

    /**
     * Prints a matrix
     * @param mat   matrix to print
     */
    public static void printMat(float[][] mat)
    {
        for (int i = 0; i < getY(mat); i++)
        {
            printRow(mat, i);
        }
    }

    /**
     * Prints a row of a matrix
     * @param mat   matrix to print row of
     * @param y     y-value of row
     */
    public static void printRow(float[][] mat, int y)
    {
        float[] row = getRow(mat, y);
        String out  = "";

        for (float i : row)
        {
            out += i + " ";
        }
        System.out.println(out);
    }

    /**
     * Gets modular inverse of a value.
     * For text, the modulo value should be 97
     * @param co    value to find modular inverse of with given modulo
     * @param mod   modulo
     * @return      modular inverse of co (mod mod)
     */
    public static int modInv(int co, int mod)
    {
        boolean found = false;
        int mult      = 0;
        int com       = co;

        while (!found)
        {
            com   = mult * co;
            int m = com % mod;

            while (m < 0)
            {
                m += mod;
            }
            if (m == 1) found = true;
            else mult++;
        }
        return mult;
    }
}