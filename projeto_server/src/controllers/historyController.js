import * as HistoryService from '../services/historyService.js';

export const getHistory = async (req, res) => {
  const userId = req.user.username;
  console.log(userId, req.user, req.params, req.body)


  try {
    const history = await HistoryService.getHistory(userId);
    console.log("Success!", history)
    res.send({ status: "OK", data: { history } });
  } catch (error) {
    console.error(error)
    res.status(error?.status || 500).send({ status: "FAILED", data: { error: error.message } });
  }
};

export const addHistory = async (req, res) => {
  const userId = req.user.username;
  const gameHistory = req.body.history;
  console.log(userId, req.user, req.params, req.body)

  try {
    await HistoryService.addHistory(userId, gameHistory);
    console.log("Success!")
    res.status(201).send({ status: "OK" });
  } catch (error) {
    console.error(error)
    res.status(error?.status || 500).send({ status: "FAILED", data: { error: error.message } });
  }
};
