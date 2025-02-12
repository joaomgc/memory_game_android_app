import * as UserService from "../services/userService.js"
import * as ScoreService from "../services/scoreService.js"

export const createNewUser = async (req, res, next) => {
  const { body } = req;

  if (
    !body.username ||
    !body.password ||
    !body.role
  ) {
    res
      .status(400)
      .send({
        status: "FAILED",
        data: {
          error:
            "One of the following keys is missing or is empty in request body: 'username', 'password', 'role'",
        },
      });
    return;
  }

  const newUser = body;

  try {
    const createdUser = await UserService.createNewUser(newUser);
    res.status(201).send({ status: "OK", data: { user: createdUser } });
  } catch (error) {
    res
      .status(error?.status || 500)
      .send({ status: "FAILED", data: { error: error?.message || error } });
  }
};

export const getAllUsers = async (req, res, next) => {
  try {
    const allUsers = await UserService.getAllUsers();
    res.send({ status: "OK", data: { users: allUsers } });
  } catch (error) {
    res
      .status(error?.status || 500)
      .send({ status: "FAILED", data: { error: error?.message || error } });
  }
};

export const getOneUser = async (req, res, next) => {
  try {
    const user = await UserService.getOneUser(req.params.username);
    res.send({ status: "OK", data: { user } });
  } catch (error) {
    res
      .status(error?.status || 500)
      .send({ status: "FAILED", data: { error: error?.message || error } });
  }
};

export const updateOneUser = async (req, res, next) => {
  try {
    const user = await UserService.updateOneUser(req.params.username, req.body);
    res.send({ status: "OK", data: { user } });
  } catch (error) {
    res
      .status(error?.status || 500)
      .send({ status: "FAILED", data: { error: error?.message || error } });
  }
};

export const deleteOneUser = async (req, res, next) => {
  try {
    await UserService.deleteOneUser(req.params.username);
    res.send({ status: "OK" });
  } catch (error) {
    res
      .status(error?.status || 500)
      .send({ status: "FAILED", data: { error: error?.message || error } });
  }
};

export const getPersonalScores = async (req, res, next) => {
  const { username } = req.params;
  try {
    const user = await UserService.getOneUser(username);
    console.log("getPersonalScores success!", user.scores)
    // getOneUser will throw the exception if not found, no need to check for null
    res.send({ status: "OK", data: { scores: user.scores } });
  } catch (error) {
    console.error("getPersonalScores ", error)
    res.status(error?.status || 500).send({ status: "FAILED", data: { error: error.message } });
  }
};

export const addPersonalScore = async (req, res, next) => {
  const { username } = req.params;
  const { moves, time, board } = req.body;
  console.log("addPersonalScore req.body = ", req.body)
  console.log("addPersonalScore username = ", username, req.params)
  try {
    await UserService.addPersonalScore(username, board, { moves, time });
    await ScoreService.addGlobalScore(board, { username, moves, time });
    console.log("Success in addPersonalScore!")
    res.status(201).send({ status: "OK" });
  } catch (error) {
    console.error("Error in addPersonalScore: ", error)
    res.status(error?.status || 500).send({ status: "FAILED", data: { error: error.message } });
  }
};
