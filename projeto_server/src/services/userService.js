import * as User from '../database/user.js'
import bcrypt from 'bcryptjs';
import { db, admin } from '../database/firebase.js';

const generatePasswordHash = async (password) =>  await bcrypt.hash(password, 12);

export const getAllUsers = async () => {
  try {
    const allUsers = await User.getAllUsers();
    return allUsers;
  } catch (error) {
    throw error;
  }
};

export const getOneUser = async (username) => {
  try {
    const user = await User.getOneUser(username);
    return user;
  } catch (error) {
    throw error;
  }
};

export const createNewUser = async (newUser) => {
  try {
    const hashedPassword = await generatePasswordHash(newUser.password);
    const userToInsert = {
      ...newUser,
      password: hashedPassword
    };
    const createdUser = await User.createNewUser(userToInsert);
    return createdUser;
  } catch (error) {
    throw error;
  }
};

export const updateOneUser = async (username, changes) => {
  try {
    if (changes.password) {
      const hashedPassword = await generatePasswordHash(changes.password);
      changes = { ...changes, password: hashedPassword };
    }
    const updatedUser = await User.updateOneUser(username, changes);
    return updatedUser;
  } catch (error) {
    throw error;
  }
};

export const deleteOneUser = async (username) => {
  try {
    await User.deleteOneUser(username);
  } catch (error) {
    throw error;
  }
};


export const addPersonalScore = async (username, board, newScore) => {
  const userDocRef = db.collection('users').doc(username);
  const userDoc = await userDocRef.get();

  if (!userDoc.exists) {
    throw new Error("User not found");
  }

  const userData = userDoc.data();
  const currentBestScore = userData.scores?.[board];

  console.log(`Current best score for board ${board}:`, currentBestScore);
  console.log(`Attempting to add/update score:`, newScore);

  await userDocRef.update({
    [`scores.${board}`]: newScore
  });
  console.log(`Score updated for ${username} on board ${board}:`, newScore);
};