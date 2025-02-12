import { db, admin } from '../database/firebase.js';

export const addGlobalScore = async (board, score) => {
  const boardRef = db.collection('globalScores').doc(board);
  const doc = await boardRef.get();

  if (!doc.exists) {
    await boardRef.set({
      scores: [score]
    });
  } else {
    const existingScores = doc.data().scores || [];
    const userScoreIndex = existingScores.findIndex(s => s.username === score.username);

    if (userScoreIndex !== -1) {
        existingScores[userScoreIndex] = score;
    } else {
      existingScores.push(score);
    }

    await boardRef.update({ scores: existingScores });
  }
};

export const getGlobalScores = async () => {
  const scoresSnapshot = await db.collection('globalScores').get();
  const scores = {};

  scoresSnapshot.docs.forEach(doc => {
    scores[doc.id] = doc.data().scores; // Use document ID as the key
  });

  return scores;
};

// Erase all data, it's dangerous
async function clearGlobalScores() {
  const boardRefs = db.collection('globalScores');
  const boardsSnapshot = await boardRefs.get();

  for (const boardDoc of boardsSnapshot.docs) {
      await boardDoc.ref.update({
          scores: admin.firestore.FieldValue.delete()
      });
  }

  console.log("All global scores have been cleared.");
}
