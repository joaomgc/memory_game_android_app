import express from 'express';
import { withAuth } from '../middlewares/authMiddleware.js';
import * as ScoreController from '../controllers/scoreController.js';

const router = express.Router();

//router.use(withAuth);

// Global scores routes
router.get('/global', ScoreController.getGlobalScores);
router.post('/global', ScoreController.addGlobalScore);


export default router;