import { db } from "./firebase.js";

export const getAllUsers = async () => {
  try {
    const users = await db.collection('users').get();
    return users.docs.map((doc) => ({ username: doc.id, ...doc.data() }));
  } catch (error) {
    throw { status: 500, message: error };
  }
};

export const getOneUser = async (username) => {
  try {
    const userDoc = await db.collection('users').doc(username).get();
    if (!userDoc.exists) {
      throw {
        status: 400,
        message: `Can't find user with the username '${username}'`
      };
    }
    return { username, ...userDoc.data() };
  } catch (error) {
    throw { status: error?.status || 500, message: error?.message || error };
  }
};

export const createNewUser = async (newUser) => {
  try {
    const { username } = newUser;
    const userDocRef = db.collection('users').doc(username);
    if ((await userDocRef.get()).exists) {
      throw {
        status: 400,
        message: `User with the username '${username}' already exists`
      };
    }
    const { username: _, ...newUserNoKey } = { ...newUser };
    await userDocRef.set(newUserNoKey);
    return { username, ...(await userDocRef.get()).data() };
  } catch (error) {
    throw { status: error?.status || 500, message: error?.message || error };
  }
}

export const updateOneUser = async (username, changes) => {
  try {
    const { username: _, ...changesNoKey } = { ...changes };
    const userDocRef = db.collection('users').doc(username);
    if (!(await userDocRef.get()).exists) {
      throw {
        status: 400,
        message: `Can't find user with the username '${username}'`
      };
    }
    await userDocRef.update(changesNoKey);
    return { username, ...(await userDocRef.get()).data() };
  } catch (error) {
    throw { status: error?.status || 500, message: error?.message || error };
  }
};

export const deleteOneUser = async (username) => {
  try {
    const userDocRef = db.collection('users').doc(username);
    if (!(await userDocRef.get()).exists) {
      throw {
        status: 400,
        message: `Can't find user with the username '${username}'`
      };
    }
    await userDocRef.delete();
  } catch (error) {
    throw { status: error?.status || 500, message: error?.message || error };
  }
};
