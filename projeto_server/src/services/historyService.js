import { db } from '../database/firebase.js';

export const addHistory = async (userId, newGameHistoryEntries) => {
  if (!userId) {
    throw new Error("User not logged in.");
  }

  const userRef = db.collection('users').doc(userId);

  try {
    const doc = await userRef.get();
    let gameHistoryData = [];

    if (doc.exists && doc.data().gameHistory) {
      gameHistoryData = [...doc.data().gameHistory];
    }

    gameHistoryData.push(...newGameHistoryEntries.map(entry => ({
      date: entry.date,
      boardSize: entry.boardSize,
      moves: entry.moves,
      timeInSeconds: entry.timeInSeconds
    })));

    await userRef.update({ gameHistory: gameHistoryData });

    console.log("Game history successfully updated on the server.");
  } catch (e) {
    throw new Error(`Error updating game history on the server: ${e.message}`);
  }
};


// Função para buscar o histórico do Firestore
export const getHistory = async (userId) => {
  if (!userId) {
    throw new Error("User not logged in.");
  }

  const userRef = db.collection('users').doc(userId);

  try {
    const snapshot = await userRef.get();

    if (!snapshot.exists) {
      return [];
    }

    const gameHistoryArray = snapshot.data().gameHistory || [];

    return gameHistoryArray.map(gameEntry => ({
      date: gameEntry.date,
      boardSize: gameEntry.boardSize,
      moves: gameEntry.moves,
      timeInSeconds: gameEntry.timeInSeconds
    }));
  } catch (e) {
    throw new Error(`Error fetching game history: ${e.message}`);
  }
};
