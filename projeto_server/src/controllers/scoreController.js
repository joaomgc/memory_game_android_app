import * as ScoreService from '../services/scoreService.js';
import * as UserService from '../services/userService.js';


export const getGlobalScores = async (req, res) => {
  try {
    const scores = await ScoreService.getGlobalScores();
    console.log("getGlobalScore success getted global score! ", scores)
    res.send({ status: "OK", data: { scores } });
  } catch (error) {
    res.status(error?.status || 500).send({ status: "FAILED", data: { error: error.message } });
  }
};

export const addGlobalScore = async (req, res) => {
  const { username, moves, time, board } = req.body;
  console.log("addGlobalScore Payload sent: ", req.body)
  console.log("addGlobalScore Params sent: ", req.params)
  try {
    await ScoreService.addGlobalScore(board, { username, moves, time });
    console.log("addGlobalScore success added global score! ", board, {username, moves, time})
    res.status(201).send({ status: "OK" });
  } catch (error) {
    console.error("addGlobalScore error in addGlobalScore:", error)
    res.status(error?.status || 500).send({ status: "FAILED", data: { error: error.message } });
  }
};
