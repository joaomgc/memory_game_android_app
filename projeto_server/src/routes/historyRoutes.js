import express from 'express';
import { withAuth } from '../middlewares/authMiddleware.js'; // Middleware de autenticação
import * as HistoryController from '../controllers/historyController.js';

const router = express.Router();

router.use(withAuth);

router.get('/', withAuth, HistoryController.getHistory);
router.post('/', withAuth, HistoryController.addHistory);

export default router;