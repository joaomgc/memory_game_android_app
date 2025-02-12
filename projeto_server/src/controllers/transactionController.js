import * as TransactionService from '../services/transactionService.js';

export const createTransaction = async (req, res, next) => {
  const { body } = req;

  if (!body.amount || !body.type || !body.date) {
    res.status(400).send({
      status: "FAILED",
      data: {
        error: "One of the following keys is missing or is empty in request body: 'amount', 'type', 'date'",
      },
    });
    return;
  }

  const newTransaction = {
    amount: body.amount,
    type: body.type,
    date: body.date,
  };

  try {
    const createdTransaction = await TransactionService.createNewTransaction(newTransaction);
    res.status(201).send({ status: "OK", data: { transaction: createdTransaction } });
  } catch (error) {
    res.status(error?.status || 500).send({ status: "FAILED", data: { error: error?.message || error } });
  }
};

export const getAllTransactions = async (req, res, next) => {
  try {
    const allTransactions = await TransactionService.getAllTransactions();
    res.send({ status: "OK", data: { transactions: allTransactions } });
  } catch (error) {
    res.status(error?.status || 500).send({ status: "FAILED", data: { error: error?.message || error } });
  }
};

export const getTransactionById = async (req, res, next) => {
  try {
    const transaction = await TransactionService.getOneTransaction(req.params.id);
    res.send({ status: "OK", data: { transaction } });
  } catch (error) {
    res.status(error?.status || 500).send({ status: "FAILED", data: { error: error?.message || error } });
  }
};

export const updateTransaction = async (req, res, next) => {
  try {
    const updatedTransaction = await TransactionService.updateOneTransaction(req.params.id, req.body);
    res.send({ status: "OK", data: { transaction: updatedTransaction } });
  } catch (error) {
    res.status(error?.status || 500).send({ status: "FAILED", data: { error: error?.message || error } });
  }
};

export const deleteTransaction = async (req, res, next) => {
  try {
    await TransactionService.deleteOneTransaction(req.params.id);
    res.send({ status: "OK" });
  } catch (error) {
    res.status(error?.status || 500).send({ status: "FAILED", data: { error: error?.message || error } });
  }
};


export const giveReward = async (req, res, next) => {
  try {
    const { username } = req.user;
    const { amount, description } = req.body;

    if (!amount || !description) {
      return res.status(400).json({ status: "FAILED", message: "Missing required fields" });
    }

    const result = await TransactionService.giveReward(username, amount, description);
    res.status(200).json({ status: "OK", data: result });
  } catch (error) {
    res.status(500).json({ status: "FAILED", message: error?.message || error });
  }
};

export const payCoin = async (req, res, next) => {
  try {
    const { username } = req.user;
    const amount = 1;
    const description = "Payment";

    console.log("Paying coin...")
    const result = await TransactionService.payCoin(username, amount, description);
    console.log("Success paying coin!")
    res.status(200).json({ status: "OK", data: result });
  } catch (error) {
    console.error("Error paying coin!", error)
    res.status(500).json({ status: "FAILED", message: error?.message || error });
  }
};