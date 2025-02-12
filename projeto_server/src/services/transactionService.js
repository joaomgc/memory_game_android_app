import * as Transaction from '../database/transaction.js'

export const getAllTransactions = async () => {
  try {
    const allTransactions = await Transaction.getAllTransactions();
    return allTransactions;
  } catch (error) {
    throw error;
  }
};

export const getOneTransaction = async (id) => {
  try {
    const transaction = await Transaction.getOneTransaction(id);
    return transaction;
  } catch (error) {
    throw error;
  }
};

export const createNewTransaction = async (newTransaction) => {
  try {
    const createdTransaction = await Transaction.createNewTransaction(newTransaction);
    return createdTransaction;
  } catch (error) {
    throw error;
  }
};

export const updateOneTransaction = async (id, changes) => {
  try {
    const updatedTransaction = await Transaction.updateOneTransaction(id, changes);
    return updatedTransaction;
  } catch (error) {
    throw error;
  }
};

export const deleteOneTransaction = async (id) => {
  try {
    await Transaction.deleteOneTransaction(id);
  } catch (error) {
    throw error;
  }
};

export const giveReward = async (username, amount, description) => {
  try {
    // Call the `giveReward` function in the database layer
    const result = await Transaction.giveReward(username, amount, description);
    return result;
  } catch (error) {
    throw error;
  }
};

export const payCoin = async (username, amount, description) => {
  try {
    // Call the `payCoin` function in the database layer
    const result = await Transaction.payCoin(username, amount, description);
    return result;
  } catch (error) {
    throw error;
  }
};
