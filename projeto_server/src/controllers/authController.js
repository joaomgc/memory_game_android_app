import * as AuthService from "../services/authService.js"

export const login = async (req, res, next) => {
  try {
    const { username, password } = req.body;

    if (!username || !password) {
      res
        .status(400)
        .send({
          status: "FAILED",
          data: {
            error:
              "One of the following keys is missing or is empty in request body: 'username', 'password'",
          },
        });
      return;
    }

    const { token } = await AuthService.loginUser(username, password);

    res.send({ status: "OK", data: { token } });
  } catch (error) {
    res
      .status(error?.status || 500)
      .send({ status: "FAILED", data: { error: error?.message || error } });
  }
};

export const refresh = async (req, res, next) => {
  try {
    const user = req.user; // Populated by middleware (AuthMiddlewre.withAuth)

    const { token } = await AuthService.refreshToken(user);

    res.send({ status: "OK", data: { token } });
  } catch (error) {
    res
      .status(error?.status || 500)
      .send({ status: "FAILED", data: { error: error?.message || error } });
  }
};
