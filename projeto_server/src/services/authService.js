import bcrypt from 'bcryptjs';
import { generateToken } from '../utils/tokenUtils.js';
import * as UserService from '../services/userService.js';

const invalidCredentialsError = {
  status: 401,
  message: "Invalid credentials"
};

export const loginUser = async (username, password) => {
  // Get user
  const user = await UserService.getOneUser(username);
  if (!user) {
    throw invalidCredentialsError;
  }

  // Verify password
  const isPasswordCorrect = await bcrypt.compare(password, user.password);
  if (!isPasswordCorrect) {
    throw invalidCredentialsError;
  }

  // Generate token
  const payload = { username: user.username, role: user.role };
  const token = generateToken(payload);

  return { token, user };
};

export const refreshToken = async (user) => {
  // Generate a new access token using the user's payload
  const payload = { username: user.username, role: user.role };
  const newToken = generateToken(payload);

  return { token: newToken };
};
