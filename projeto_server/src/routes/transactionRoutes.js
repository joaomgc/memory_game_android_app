import express from 'express';
import { withAuth } from '../middlewares/authMiddleware.js';
import * as TransactionController from '../controllers/transactionController.js';

const router = express.Router();

router.use(withAuth);

router.post('/', TransactionController.createTransaction);
router.get('/', TransactionController.getAllTransactions);
router.get('/:id', TransactionController.getTransactionById);
router.patch('/:id', TransactionController.updateTransaction);
router.delete('/:id', TransactionController.deleteTransaction);
router.post("/giveReward", TransactionController.giveReward);
router.post("/payCoin", TransactionController.payCoin);

export default router;