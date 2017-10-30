package server;

import javax.swing.tree.DefaultTreeModel;
import java.util.Arrays;

/**
 * Tree structure for information distribution for key exchange
 *
 * @author Caroline Zeng
 * @version 1.0.0
 */

public class ExchangeTree extends DefaultTreeModel {
    /**
     * All Nodes in Tree
     */
    int nodes;
    /**
     * Starter information (base modulo pair) for Tree
     */
    int[] inf;
    /**
     * Root Node
     */
    ExchangeTreeNode root;

    /**
     * Constructor for ExchangeTree
     * @param root  root node
     * @param info  Server's public information (base and mod)
     */
    public ExchangeTree(ExchangeTreeNode root, int[] info)
    {
        super(root);
        this.root = root;

        // Make sure Sockets of all ExchangeThreads stay open
        lockAll(root);
        // Create Tree; distribute Server ExchangeThreads to branches and leaves
        distributeThreads(root);

        nodes = countNodes(root);
        inf   = info;
    }

    /**
     * Carry out key exchange
     */
    public void doExchange()
    {
        // Tree is made up only of root node; only process within itself
        if (nodes == 1)
        {
            finish(inf, root);
        } else
        {
            // Do exchange in each child branch
            int[] temp1 = doe(inf, (ExchangeTreeNode) root.getFirstChild());
            int[] temp2 = doe(inf, (ExchangeTreeNode) root.getLastChild());

            // Pass each branch's information to the other branch, and finish the exchange
            finish(temp1, (ExchangeTreeNode) root.getLastChild());
            finish(temp2, (ExchangeTreeNode) root.getFirstChild());

        }
    }

    /**
     * Send information through all nodes in a branch
     * @param inf   information to process
     * @param n     node to process
     * @return      if node isn't a leaf, result from the branch
     *              else, result from node
     */
    public int[] doe(int[] inf, ExchangeTreeNode n)
    {
        // If the node is a leaf (i.e. no children), process within self
        if (n.isLeaf())
        {
            n.processSelf(inf);
            return n.info;
        } else
        {
            // Process one branch
            int[] temp = doe(inf, (ExchangeTreeNode) n.getFirstChild());
            // Pass resulting information to other branch, and process in other branch
            return       doe(temp, (ExchangeTreeNode) n.getLastChild());
        }
    }

    /**
     * Finish the exchange in the branch
     * Make sure private key never passes through Server side
     * @param inf   information to process
     * @param n     node to be processed
     */
    public void finish(int[] inf, ExchangeTreeNode n)
    {
        // If the node is a leaf (i.e. no children), process within self
        if (n.isLeaf())
        {
            n.finish(inf);
        } else
        {
            // Do exchange in each child branch independently
            int[] temp1 = doe(inf, (ExchangeTreeNode) n.getFirstChild());
            int[] temp2 = doe(inf, (ExchangeTreeNode) n.getLastChild());

            // Pass each branch's information to the other branch, and finish each branch independently
            finish(temp1, (ExchangeTreeNode) n.getLastChild());
            finish(temp2, (ExchangeTreeNode) n.getFirstChild());
        }
    }

    /**
     * Count the number of nodes below a node
     * @param root  parent node
     * @return      number of nodes below parent node
     */
    public int countNodes(ExchangeTreeNode root)
    {
        // Count the nodes in the binary tree to which
        // root points, and return the answer.
        if (root == null)
            return 0;  // The tree is empty.  It contains no nodes.
        else if (root.isLeaf()) return 1;
        else
        {
            int count = 1;   // Start by counting the root.
            count += countNodes((ExchangeTreeNode) root.getFirstChild());
            // Add the number of nodes
            //     in the left subtree.
            count += countNodes((ExchangeTreeNode) root.getLastChild());
            // Add the number of nodes
            //    in the right subtree.
            return count;  // Return the total.
        }
    }

    /**
     * Distribute the ExchangeThreads of node n to children
     * @param n     parent node
     */
    public void distributeThreads(ExchangeTreeNode n)
    {
        float size = (float) (n.exchangeThreads.length / 2.0);
        if (size > 1)
        {
            // Splits ArrayList of Threads into approximate halves, then pass to child nodes
            ServerExchangeThread[] e1 = Arrays.copyOfRange(n.exchangeThreads, 0, (int) size);
            ServerExchangeThread[] e2 = Arrays.copyOfRange(n.exchangeThreads, (int) size,
                    n.exchangeThreads.length);

            ExchangeTreeNode n1 = new ExchangeTreeNode(e1);
            ExchangeTreeNode n2 = new ExchangeTreeNode(e2);

            distributeThreads(n1);
            distributeThreads(n2);

            n.add(n1);
            n.add(n2);
        }
    }

    /**
     * Lock all ReentrantLocks of ExchangeThreads of node n
     * Keep Sockets open
     * @param n     parent node
     */
    public void lockAll(ExchangeTreeNode n)
    {
        for (int i = 0; i < n.exchangeThreads.length; i++)
        {
            n.exchangeThreads[i].lock.lock();
        }
    }

    /**
     * Unlock all ReentrantLocks of ExchangeThreads of node n
     * Allow Sockets to close
     * @param n     parent node
     */
    public void unlockAll(ExchangeTreeNode n)
    {
        for (int i = 0; i < n.exchangeThreads.length; i++)
        {
            n.exchangeThreads[i].lock.unlock();
        }
    }
}