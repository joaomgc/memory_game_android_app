import express from 'express';
import authRoutes from './authRoutes.js';
import userRoutes from './userRoutes.js';
import transactionRoutes from './transactionRoutes.js';
import scoreRoutes from './scoreRoutes.js';
import historyRoutes from './historyRoutes.js';

const router = express.Router();

router.use('/auth', authRoutes);
router.use('/users', userRoutes);
router.use('/transactions', transactionRoutes);
router.use('/scores', scoreRoutes);
router.use('/histories', historyRoutes);

export default router;
