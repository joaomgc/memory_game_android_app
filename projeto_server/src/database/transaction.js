import { db } from "./firebase.js";

export const getAllTransactions = async () => {
  try {
    const transactions = await db.collection('transactions').get();
    return transactions.docs.map((doc) => ({ id: doc.id, ...doc.data() }));
  } catch (error) {
    throw { status: 500, message: error };
  }
};

export const getOneTransaction = async (id) => {
  try {
    const transactionDoc = await db.collection('transactions').doc(id).get();
    if (!transactionDoc.exists) {
      throw {
        status: 400,
        message: `Can't find transaction with the id '${id}'`
      };
    }
    return { id, ...transactionDoc.data() };
  } catch (error) {
    throw { status: error?.status || 500, message: error?.message || error };
  }
};


export const createNewTransaction = async (newTransaction) => {
  try {
    const transactionDocRef = await db.collection('transactions').add(newTransaction);
    const newTransactionDoc = await transactionDocRef.get();
    return { id: transactionDocRef.id, ...newTransactionDoc.data() };
  } catch (error) {
    throw { status: error?.status || 500, message: error?.message || error };
  }
};

export const updateOneTransaction = async (id, changes) => {
  try {
    const { id: _, ...changesNoKey } = { ...changes };
    const transactionDocRef = db.collection('transactions').doc(id);
    if (!(await transactionDocRef.get()).exists) {
      throw {
        status: 400,
        message: `Can't find transaction with the id '${id}'`
      };
    }
    await transactionDocRef.update(changesNoKey);
    return { id, ...(await transactionDocRef.get()).data() };
  } catch (error) {
    throw { status: error?.status || 500, message: error?.message || error };
  }
};

export const deleteOneTransaction = async (id) => {
  try {
    const transactionDocRef = db.collection('transactions').doc(id);
    if (!(await transactionDocRef.get()).exists) {
      throw {
        status: 400,
        message: `Can't find transaction with the id '${id}'`
      };
    }
    await transactionDocRef.delete();
  } catch (error) {
    throw { status: error?.status || 500, message: error?.message || error };
  }
};

export const giveReward = async (username, amount, description) => {
  const userRef = db.collection('users').doc(username);

  return await db.runTransaction(async (transaction) => {
    const userDoc = await transaction.get(userRef);

    if (!userDoc.exists) {
      throw new Error("User not found");
    }

    const userData = userDoc.data();
    const currentCoins = userData.coins || 0;

    // Update coins
    const updatedCoins = currentCoins + amount;
    transaction.update(userRef, { coins: updatedCoins });

    // Update transaction history
    const transactionHistory = userData.transactionHistory || [];
    const newTransaction = {
      date: new Date().toISOString().split('T')[0], // Format YYYY-MM-DD
      description,
      value: amount,
    };
    transaction.update(userRef, {
      transactionHistory: [...transactionHistory, newTransaction],
    });

    return { username, updatedCoins, newTransaction };
  });
};

export const payCoin = async (username, amount, description) => {
  const userRef = db.collection('users').doc(username);

  return await db.runTransaction(async (transaction) => {
    const userDoc = await transaction.get(userRef);

    if (!userDoc.exists) {
      throw new Error("User not found");
    }

    const userData = userDoc.data();
    const currentCoins = userData.coins || 0;

    if (currentCoins < amount) {
      throw new Error("Insufficient coins");
    }

    // Update coins
    const updatedCoins = currentCoins - amount;
    transaction.update(userRef, { coins: updatedCoins });

    // Update transaction history
    const transactionHistory = userData.transactionHistory || [];
    const newTransaction = {
      date: new Date().toISOString().split('T')[0], // Format YYYY-MM-DD
      description,
      value: -amount,
    };
    transaction.update(userRef, {
      transactionHistory: [...transactionHistory, newTransaction],
    });

    return { username, updatedCoins, newTransaction };
  });
};