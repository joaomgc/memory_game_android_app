import express from 'express';
import { withAuth, withRole } from '../middlewares/authMiddleware.js';
import * as UserController from '../controllers/userController.js';

const router = express.Router();

//router.use(withAuth, withRole(["admin"]));

router.post('/', UserController.createNewUser);
router.get('/', UserController.getAllUsers);
router.get('/:username', UserController.getOneUser);
router.patch('/:username', UserController.updateOneUser);
router.delete('/:username', UserController.deleteOneUser);

router.get('/:username/scores', UserController.getPersonalScores);
router.post('/:username/scores', UserController.addPersonalScore);


export default router;
