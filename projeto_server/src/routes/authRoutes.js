import express from 'express';
import { withAuth } from '../middlewares/authMiddleware.js';
import * as AuthController from '../controllers/authController.js';

const router = express.Router();

router.post('/login', AuthController.login);
router.get('/refresh', withAuth, AuthController.refresh);

export default router;
